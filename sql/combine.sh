#!/bin/bash
# 用于将IDEA导出的不同表sql文件，整合成一份

# Get the current date in the format yyyymmdd
current_date=$(date +%Y%m%d)

# Create the new file name
new_file="healthtrail-${current_date}.sql"

# Check if the new file already exists
if [[ -f "$new_file" ]]; then
  # Remove the existing file
  rm "$new_file"
fi

# Loop through all .sql files in the current directory
for file in *.sql
do
  # 统一移除 SQL dump 中的反引号 schema 前缀，避免当前数据库名调整后脚本仍绑定具体库名。
  sed -E 's/`[[:alnum:]_-]+`\./ /g' "$file" >> "$new_file"
  echo "" >> "$new_file"
done

echo "Concatenation complete. Output file: $new_file"
