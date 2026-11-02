# Lab 2 baseline

Output of the released Lab 2 build, used to check behaviour preservation in Lab 3.

- Tag: `v1.1.0`
- Commit: `8fbd1f1`
- Tests: 358
- JAR: `lab01-1.1.0.jar`, sha256 `f9022bb60e0d3c8e8fd2722b6f8c91211484ef39a102279c26e328a68d83eb6a`
- Input: `data/input.csv`

Files: `report.txt` (report file), `console.txt` (stdout), `exit-code.txt`. Line endings are LF.

Commands, from the repository root:

```
gh release download v1.1.0 -D <dir>
sha256sum <dir>/lab01-1.1.0.jar
java -Dfile.encoding=UTF-8 -jar <dir>/lab01-1.1.0.jar --input data/input.csv --output docs/baseline/lab2/report.txt > docs/baseline/lab2/console.txt
echo $? > docs/baseline/lab2/exit-code.txt
```

The output is identical to `docs/baseline/lab1/`.
