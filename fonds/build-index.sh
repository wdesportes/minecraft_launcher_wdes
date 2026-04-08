#!/usr/bin/env bash
# build_index.sh
# Scans a directory and builds index.json in the format expected by Fonds/Fond.
#
# Usage: ./build_index.sh <directory> [output_index.json]
#   directory          — folder of files to index
#   output_index.json  — defaults to index.json in current directory

set -euo pipefail

DIR="${1:?Usage: $0 <directory> [output_index.json]}"
OUT="${2:-index.json}"

[[ -d "$DIR" ]] || { echo "ERROR: '$DIR' is not a directory."; exit 1; }

sha1() {
  if command -v sha1sum &>/dev/null; then
    sha1sum "$1" | awk '{print $1}'
  else
    shasum -a 1 "$1" | awk '{print $1}'
  fi
}

md5() {
  if command -v md5sum &>/dev/null; then
    md5sum "$1" | awk '{print $1}'
  else
    md5 -q "$1"
  fi
}

echo "Indexing files in: $DIR"

# Build a stream of jq key-value pairs and merge into one object
result='{}'

while IFS= read -r -d '' file; do
  name="$(basename "$file")"

  if [ "$name" == "index.json" ] || [ "$name" == "build-index.sh" ]; then
   continue
  fi

  key="${file#"$DIR"}"
  size=$(stat -c%s "$file" 2>/dev/null || stat -f%z "$file")
  hash=$(sha1 "$file")
  md5val=$(md5 "$file")

  echo "  + $key ($size bytes)"

  result=$(jq \
    --arg key "$key" \
    --arg name "$name" \
    --arg md5 "$md5val" \
    --arg hash "$hash" \
    --argjson size "$size" \
    '. + {
      ($key): {
        name: $name,
        MD5: $md5,
        hash: $hash,
        size: $size,
        compressedHash: null,
        compressedSize: 0
      }
    }' <<< "$result")

done < <(find "$DIR" -type f -print0 | sort -z)

jq '{ fonds: . }' <<< "$result" > "$OUT"

COUNT=$(jq 'length' "$OUT")
echo "Done. $COUNT file(s) written to $OUT"
