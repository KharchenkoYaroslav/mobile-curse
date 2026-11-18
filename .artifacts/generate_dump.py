import os

root_dir = r"C:/education/5sem/mobile/code"
output_file = os.path.join(root_dir, "client_files.txt")

client_paths = [
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle/libs.versions.toml",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java",
    "app/src/main/res",
    "shared/build.gradle.kts",
    "shared/src/main",
]

text_extensions = {".kt", ".kts", ".xml", ".toml", ".properties", ".txt", ".md", ".keep"}

files_to_dump = []

for cp in client_paths:
    full_path = os.path.join(root_dir, cp)
    if os.path.isfile(full_path):
        files_to_dump.append(full_path)
    elif os.path.isdir(full_path):
        for root, dirs, files in os.walk(full_path):
            for file in files:
                ext = os.path.splitext(file)[1].lower()
                if ext in text_extensions:
                    files_to_dump.append(os.path.join(root, file))

with open(output_file, "w", encoding="utf-8") as out:
    for fpath in sorted(files_to_dump):
        rel_path = os.path.relpath(fpath, root_dir)
        out.write("=" * 80 + "\n")
        out.write(f"FILE: {rel_path}\n")
        out.write("=" * 80 + "\n\n")
        try:
            with open(fpath, "r", encoding="utf-8") as f:
                out.write(f.read())
        except Exception as e:
            out.write(f"[Error reading file: {e}]\n")
        out.write("\n\n")

print(f"Successfully generated {output_file} with {len(files_to_dump)} files.")
