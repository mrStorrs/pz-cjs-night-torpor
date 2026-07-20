#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "${script_dir}/.." && pwd)"
build_dir="${project_dir}/build"
classes_dir="${build_dir}/classes"
test_classes_dir="${build_dir}/test-classes"
output_jar="${project_dir}/42/media/java/CJSNightTorpor.jar"

pz_jar="${PZ_JAR:-/home/cjstorrs/games/Project Zomboid Linux 42.19.0/game/projectzomboid/projectzomboid.jar}"
pz_java="${PZ_JAVA:-$(dirname "${pz_jar}")/jre64/bin/java}"
zombie_buddy_jar="${ZOMBIE_BUDDY_JAR:-/home/cjstorrs/Zomboid/mods/ZombieBuddy/libs/ZombieBuddy.jar}"
ecj_jar="${ECJ_JAR:-${project_dir}/.tools/ecj.jar}"
if [[ ! -f "${ecj_jar}" && -f "${project_dir}/../cjsStealthOverhaul/.tools/ecj.jar" ]]; then
    ecj_jar="${project_dir}/../cjsStealthOverhaul/.tools/ecj.jar"
fi

for required_file in "${pz_jar}" "${zombie_buddy_jar}" "${ecj_jar}"; do
    if [[ ! -f "${required_file}" ]]; then
        echo "Missing build dependency: ${required_file}" >&2
        exit 1
    fi
done
if [[ ! -x "${pz_java}" ]]; then
    echo "Missing Project Zomboid Java runtime: ${pz_java}" >&2
    exit 1
fi
if [[ ! -f "${project_dir}/42/poster.png" ]]; then
    echo "Missing B42 poster: ${project_dir}/42/poster.png" >&2
    exit 1
fi

mkdir -p "${classes_dir}" "${test_classes_dir}" "$(dirname "${output_jar}")"
find "${classes_dir}" "${test_classes_dir}" -type f -delete

mapfile -t main_sources < <(find "${project_dir}/java/src/main/java" -name '*.java' -type f | sort)
java -jar "${ecj_jar}" -21 -cp "${pz_jar}:${zombie_buddy_jar}" -d "${classes_dir}" "${main_sources[@]}"

mapfile -t test_sources < <(find "${project_dir}/java/src/test/java" -name '*.java' -type f | sort)
java -jar "${ecj_jar}" -21 -cp "${classes_dir}:${pz_jar}:${zombie_buddy_jar}" -d "${test_classes_dir}" "${test_sources[@]}"
"${pz_java}" -ea -cp "${classes_dir}:${test_classes_dir}:${pz_jar}:${zombie_buddy_jar}" \
    com.cjstorrs.cjsnighttorpor.NightTorporPatchTest

advice_classes=(
    PreserveNightSpeed
    WeakenNightHearing
)
for advice_class in "${advice_classes[@]}"; do
    invalid_references="$(
        javap -classpath "${classes_dir}" -c "com.cjstorrs.cjsnighttorpor.NightTorporPatches\$${advice_class}" \
            | grep 'Method com/cjstorrs/cjsnighttorpor/' \
            | grep -v 'NightTorporRuntime\.' \
            || true
    )"
    if [[ -n "${invalid_references}" ]]; then
        echo "ZombieBuddy advice ${advice_class} references a non-facade helper:" >&2
        echo "${invalid_references}" >&2
        exit 1
    fi
done

jar --create --file "${output_jar}" --date=2000-01-01T00:00:00Z -C "${classes_dir}" .
if jar --list --file "${output_jar}" | grep -Eq '^(zombie|me/zed_0xff)/'; then
    echo "Game or ZombieBuddy classes leaked into ${output_jar}" >&2
    exit 1
fi

echo "Built ${output_jar}"
