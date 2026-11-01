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
