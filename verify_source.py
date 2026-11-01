"""Structural checks only. Does not compile Java or replace Android lint/device tests."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parent
for f in root.glob("app/src/main/**/*.xml"):
    ET.parse(f)
manifest=ET.parse(root/"app/src/main/AndroidManifest.xml").getroot()
ns="{http://schemas.android.com/apk/res/android}"
for tag in ("activity","service"):
    for item in manifest.findall("application/"+tag):
        name=item.attrib[ns+"name"].lstrip(".")
        assert (root/("app/src/main/java/vn/havanhuan/assist/"+name+".java")).exists(),name
for f in root.glob("app/src/**/*.java"):
    s=f.read_text(encoding="utf-8")
    # Remove comments and literals before checking lexical delimiter nesting.
    clean=re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*.*?\*/',"",s,flags=re.S)
    stack=[]
    for char in clean:
        if char in "([{": stack.append(char)
        elif char in ")]}":
            assert stack and {"(": ")", "[": "]", "{": "}"}[stack.pop()] == char, f"Delimiter mismatch: {f}"
    assert not stack, f"Unclosed delimiter: {f}"
print("Structural checks passed: XML, manifest classes, Java lexical delimiters. Compilation and device tests NOT run.")

# Regression checks for the three build-blocking issues found in earlier CI logs.
for java in root.glob("app/src/main/**/*.java"):
    assert not re.search(r"setOrientation\(\s*[01]\s*\)", java.read_text(encoding="utf-8")), java
base = ET.parse(root / "app/src/main/res/values/styles.xml").getroot()
assert not base.findall(".//item[@name='android:windowLightNavigationBar']")
versioned = ET.parse(root / "app/src/main/res/values-v27/styles.xml").getroot()
assert versioned.find(".//item[@name='android:windowLightNavigationBar']") is not None
tools = "{http://schemas.android.com/tools}"
usage = [item for item in manifest.findall("uses-permission") if item.get(ns + "name") == "android.permission.PACKAGE_USAGE_STATS"]
assert len(usage) == 1 and usage[0].get(tools + "ignore") == "ProtectedPermissions"
assert manifest.get(tools + "ignore") is None
print("Known lint regression checks passed; Android lint still must run on CI.")
