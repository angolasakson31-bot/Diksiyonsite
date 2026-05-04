#!/usr/bin/env python3
"""
expo-modules-gradle-plugin desteklemediği kotlinVersion değerlerini patch eder.
1.9.24 gibi yeni patch sürümleri pluginin iç haritasında bulunmuyor.
build.gradle ve gradle.properties içindeki her 1.9.x değeri 1.9.22'ye çekilir.
"""
import re
import sys
import os

android_dir = os.path.dirname(os.path.abspath(__file__)) if False else "."

# --- build.gradle patch ---
build_gradle = os.path.join(android_dir, "build.gradle")
with open(build_gradle, "r") as f:
    content = f.read()

before = content

# Hem single hem double quote: kotlinVersion = '1.9.x' veya "1.9.x"
content = re.sub(
    r"(kotlinVersion\s*=\s*)['\"]1\.9\.[0-9]+['\"]",
    r'\g<1>"1.9.22"',
    content,
)
# Classpath'deki doğrudan versiyon: kotlin-gradle-plugin:1.9.x
content = re.sub(
    r"(org\.jetbrains\.kotlin:kotlin-gradle-plugin:)1\.9\.[0-9]+",
    r"\g<1>1.9.22",
    content,
)
# kotlinVersion ext bloğunda yoksa ekle
if "kotlinVersion" not in content:
    content = content.replace(
        "buildToolsVersion",
        'kotlinVersion = "1.9.22"\n        buildToolsVersion',
        1,
    )

with open(build_gradle, "w") as f:
    f.write(content)

if before != content:
    print("build.gradle PATCHED OK")
else:
    print("WARNING: build.gradle degismedi — asagidaki icerige bak:")
    print(before[:800])
    sys.exit(1)

# --- gradle.properties patch ---
gradle_props = os.path.join(android_dir, "gradle.properties")
with open(gradle_props, "r") as f:
    gp = f.read()

before_gp = gp
gp = re.sub(r"kotlinVersion=.*", "kotlinVersion=1.9.22", gp)
if "kotlinVersion" not in gp:
    gp += "\nkotlinVersion=1.9.22\n"

with open(gradle_props, "w") as f:
    f.write(gp)

if before_gp != gp:
    print("gradle.properties PATCHED OK")

print("Patch tamamlandi.")
