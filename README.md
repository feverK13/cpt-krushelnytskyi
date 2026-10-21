# cpt-krushelnytskyi

Java 21 console application for the "Electronics store" domain. It reads a CSV file of product
records, validates every line, computes four metrics over the valid records and writes a
Ukrainian report to the console and to a file.

## Requirements

JDK 21. No Maven installation is needed: the build uses the Maven Wrapper (`mvnw`, `mvnw.cmd`).

## Build and run

| Shell | Build | Run |
|---|---|---|
| Windows (cmd, PowerShell) | `.\mvnw.cmd -B clean verify` | `java -jar target/lab01-1.1.0.jar` |
| Unix, Git Bash | `./mvnw -B clean verify` | `java -jar target/lab01-1.1.0.jar` |

Console output is always UTF-8; legacy `cmd.exe` needs `chcp 65001` to display it correctly.

## CLI options

| Option | Meaning | Default |
|---|---|---|
| `--help` | Print usage and exit | - |
| `--version` | Print version and build number and exit | - |
| `--input <path>` | Input CSV file | `data/input.csv` |
| `--output <path>` | Report file | `out/report.txt` |
| `--verbose` | Mirror log records to stderr | off |

`--help` takes precedence over every other argument, `--version` over any argument error;
neither creates files.

## Input format

UTF-8, separator `;`, no header line, one record per line. LF and CRLF line endings and a
leading BOM are accepted. Every field is trimmed before validation.

| Field | Type | Rule |
|---|---|---|
| `name` | String | not blank |
| `category` | String | not blank |
| `price` | double | plain decimal, finite, greater than 0 |
| `warrantyMonths` | int | integer, 0 or more, at most 2147483647 |
| `stock` | int | integer, 0 or more, at most 2147483647 |

Numbers must match `-?\d+(\.\d+)?`: no exponent, no `+` sign, no `f`/`d` suffix, no `NaN` or
`Infinity`, no thousands or comma separators.

```text
Ноутбук Lenovo IdeaPad 3;Ноутбуки;24999.00;24;7
```

## Validation

Checks run in this order and the first failing check wins. Every skipped line appears in the
report with its line number and reason, and processing continues.

| Check | Example message |
|---|---|
| Empty or blank line | `Рядок 1: порожній рядок` |
| Exactly 5 fields | `Рядок 2: очікується 5 полів, отримано 4` |
| Name not blank | `Рядок 3: назва: порожнє поле` |
| Category not blank | `Рядок 4: категорія: порожнє поле` |
| Price, warranty, stock present | `Рядок 5: ціна: порожнє поле` |
| Price is a plain decimal | `Рядок 6: ціна: не є числом: "1e3"` |
| Price fits into double | `Рядок 9: ціна: завелике значення: "111…"` (400 digits, shortened) |
| Price not negative | `Рядок 8: ціна: від'ємне значення: "-5"` |
| Price greater than 0 (`0`, `-0`) | `Рядок 7: ціна: має бути більше нуля: "0"` |
| Warranty/stock is a number | `Рядок 11: гарантія: не є числом: "+5"` |
| Warranty/stock has no fraction | `Рядок 10: гарантія: має бути цілим числом: "12.5"` |
| Warranty/stock not negative | `Рядок 12: запас: від'ємне значення: "-3"` |
| Warranty/stock fits into int | `Рядок 13: запас: завелике значення: "2147483648"` |

Line numbers in the examples are illustrative.

The price rule is `> 0`, not `>= 0`. This is a compliant refinement of the Lab 2 minimum rule:
Lab 1 rejected a price of 0, and allowing it would change the average price. Warranty and stock
allow 0.

## Model

The records of Lab 1 (`String[]` and primitives) are replaced by objects in `ua.lpnu.kzp.model`.

`Product` is a final, immutable class. Every invariant is checked in its constructor, so an invalid
`Product` cannot exist.

| Field | Type | Invariant |
|---|---|---|
| `name` | String | not blank, stored trimmed |
| `category` | String | not blank, stored trimmed |
| `price` | double | finite, greater than 0 |
| `warrantyMonths` | int | 0 or more |
| `stock` | int | 0 or more |

- Constructor `Product(name, category, price, warrantyMonths, stock)` checks the fields in this
  order; the first failing check wins. A `null` name or category throws `NullPointerException`;
  any other violation throws `ProductValidationException` (an `IllegalArgumentException`) that
  carries the field, the reason and the offending value.
- `Product.fromCsv(String line)` parses one line field by field: it parses a field, runs that
  field's check, then moves on. Its exception quotes the raw field text, so the skipped-line
  messages are the Lab 1 messages. The line number is added by `InputProcessor`, not by `Product`.
  A price that overflows `double` is reported as "too large"; the constructor rejects non-finite
  prices for direct callers.
- `Product.builder()` returns a `Product.Builder` with a fluent setter per field. `build()` calls
  the constructor, so validation cannot be bypassed and every call returns a new `Product`. Unset
  text fails like a `null` argument, an unset price like a price of 0, unset numbers are 0.

```java
Product product = Product.builder()
        .name("Монітор").category("Монітори").price(6499.00).warrantyMonths(36).stock(12)
        .build();
```

`WarrantyStock(int warrantyMonths, int stock)` is a record that groups the two quantities. Its
compact constructor rejects negative values; `Product.warrantyStock()` returns one, and
`MetricsCalculator` reads the warranty and the stock through it.

### Changes since Lab 1

- `RecordSplitter`, `RecordValidator` and `LineResult` are removed (they remain at tag `v1.0.0`).
  `InputProcessor` builds a `List<Product>` with `Product.fromCsv`; an invalid line is never added
  to the list and never changes the metrics.
- `MetricsCalculator` takes `List<Product>`; `ReportFormatter` is unchanged.
- The report, the metrics, the skipped-line messages and their order, the log format and the exit
  codes are the same as in Lab 1. `GoldenReportTest` compares the output for `data/input.csv` with
  the Lab 1 report; the snapshot is in [docs/baseline/lab1/](docs/baseline/lab1/).

## Output

Real output of `java -jar target/lab01-1.1.0.jar` for [data/input.csv](data/input.csv)
(run on Windows; on Unix the path is printed as `data/input.csv`):

```text
Звіт: електронний магазин
Вхідний файл: data\input.csv

Показник                                   Значення
---------------------------------------------------
Кількість коректних записів                       8
Середня ціна                           14030.62 грн
Найдовша гарантія, міс.                          36
Загальний запас, шт.                             75

Пропущено рядків: 6

Пропущені рядки:
Рядок 3: ціна: не є числом: "абв"
Рядок 5: порожній рядок
Рядок 7: запас: від'ємне значення: "-3"
Рядок 9: очікується 5 полів, отримано 4
Рядок 11: назва: порожнє поле
Рядок 13: гарантія: має бути цілим числом: "12.5"
```

The table holds the count of valid records, the average price (2 decimals, `Locale.ROOT`), the
longest warranty and the total stock (summed as `long`). The skipped-lines section lists the
skipped lines in file order. The same text is written to the `--output` file in UTF-8; missing
parent directories are created. Without valid records the table is replaced by
`Немає жодного коректного запису.`

## Exit codes

| Code | Meaning |
|---|---|
| 0 | Report written with at least one valid record; also `--help` and `--version` |
| 1 | No valid records; the report is still printed and written |
| 2 | Argument error, input file cannot be read, or report file cannot be written |

## Logging

Every run except `--help`, `--version` and argument errors writes `out/app.log` in UTF-8
(overwritten each run) through `java.util.logging`. `--verbose` mirrors the records to stderr;
otherwise stderr carries only error messages. Format:
`ISO-8601 time | LEVEL | Class.method | line=N field=X | message`, with `-` for an absent line or
field. ERROR records include the stack trace. If the log file cannot be created, a warning is
printed to stderr and the run continues.

```text
2026-10-06T22:30:27.829+03:00 | INFO | Main.run | line=- field=- | Application started, version 1.1.0
2026-10-06T22:30:27.857+03:00 | WARN | InputProcessor.process | line=3 field=price | Skipped line: not a number: "абв"
```

## Testing

`./mvnw -B clean verify` runs 358 JUnit 5 tests, many of them parameterized:

| Area | Covered |
|---|---|
| CLI parsing | defaults, every option, precedence, unknown arguments, missing values |
| Reading | LF/CRLF, BOM, blank lines, empty file, directory as input, malformed UTF-8 |
| Validation | `Product.fromCsv`: every rule, boundaries (0, 0.01, `-0`, 400-digit price, `Integer.MAX_VALUE` and one above), whitespace and tabs, first failing field, raw text in messages |
| Model | `Product` constructor and invariants, immutability, `Product.Builder`, `WarrantyStock` |
| Metrics | known datasets, single record, rounding, stock sum above `Integer.MAX_VALUE`, no records |
| Report | exact text, locale independence, UTF-8 file, parent directories, unwritable path |
| Logging | line format, stack traces, verbose mirror, handler I/O failures |
| End-to-end | `Main.run` over fixture files in `src/test/resources/fixtures` and `data/input.csv` |
| Behaviour | `GoldenReportTest`: console and report file equal the Lab 1 report, exit code 0 |

JaCoCo enforces a minimum covered ratio in `verify`: 95% instructions and 90% branches
(`pom.xml`). The HTML report is in `target/site/jacoco`; CI uploads it as `jacoco-report`.
Not covered on purpose: `Main.main`, which calls `System.exit`, and two `getParent() == null`
branches that the application's paths never reach.

## Project structure

```text
src/main/java/ua/lpnu/kzp
  Main, ConsoleStreams, AppVersion   entry point and orchestration, UTF-8 console, version info
  cli/      CliParser, CliParseResult   argument parsing
  data/     CsvReader, InputProcessor   reading lines and building the list of products
  model/    Product, WarrantyStock, ProductValidationException, RecordField, ValidationError
            the entity with Product.fromCsv, its invariants and the validation reasons
  metrics/  MetricsCalculator, Metrics   the four metrics
  report/   ReportFormatter, ReportWriter   report text and report file
  logging/  AppLogger, AppLogFormatter, WriterHandler   log file and verbose mirror
src/test/java      JUnit 5 tests
src/test/resources/fixtures   CSV files for end-to-end tests
src/test/resources/baseline   Lab 1 report compared by GoldenReportTest
ai/                agent role definitions
```

## CI and versioning

GitHub Actions runs `verify` on `ubuntu-latest`, `ubuntu-24.04-arm`, `windows-latest` and
`macos-latest` for pull requests, pushes to `main` and `v*` tags, with the Maven repository
cached. SpotBugs runs in `verify` and fails the build on findings. Each runner uploads the
shaded JAR as `jar-<os>-build-<N>`, where `N` is the workflow run number; `ubuntu-latest`
also uploads `jacoco-report`. The job fails if the tests created `out/` in the project directory.

The version is set in `pom.xml` (`1.1.0`) and a `v*` tag triggers CI. CI passes the run
number as `ci.build.number`; it goes into the JAR manifest (`Build-Number`) and into
`--version`, which prints `1.1.0 (build 12)` in CI and `1.1.0 (build local)` for local builds.
The version in `pom.xml`, in the tag `v1.1.0` and in the `--version` output is the same.

## Workflow

Every task is a GitHub Issue with acceptance criteria. A small group of related Issues is done
on one branch (`<type>/<N>-<M>-<slug>`) with Conventional Commits. Each branch goes through a
pull request with `Closes #N` and green CI, and is merged with a merge commit. Agent roles
are described in [ai/](ai/); lab reports are in [docs/](docs/).

## Labs

| Lab | Topic                          | Status                                                         |
|---|---|---|
| 1   | Deployment and data processing | Done, tag `v1.0.0`, [docs/REPORT-lab1.md](docs/REPORT-lab1.md) |
| 2   | Classes, record, encapsulation | Done, tag `v1.1.0`, [REPORT.md](REPORT.md)                     |

The Lab 1 output snapshot used to check behaviour preservation is in
[docs/baseline/lab1/](docs/baseline/lab1/).
