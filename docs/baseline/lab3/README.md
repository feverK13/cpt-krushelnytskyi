# Lab 3 baseline

Console output of the Lab 3 build on `data/input.csv`. Built from commit `1ba9a4f`, version 1.2.0.

- `output.txt`: default run; identical to `docs/baseline/lab2/console.txt`.
- `output-warranty-cost.txt`: run with `--warranty-cost`; the default report followed by the
  "Гарантійна вартість:" section.

Commands, from the repository root, after `./mvnw -B clean verify`:

```
java -Dfile.encoding=UTF-8 -cp target/classes ua.lpnu.kzp.Main --output out/report.txt > docs/baseline/lab3/output.txt
java -Dfile.encoding=UTF-8 -cp target/classes ua.lpnu.kzp.Main --output out/report.txt --warranty-cost > docs/baseline/lab3/output-warranty-cost.txt
```

Line endings are LF. The input path is printed as written on Windows (`data\input.csv`).
