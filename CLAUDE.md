# Project rules: cpt-krushelnytskyi

Java 21 console application, domain "Electronics store".
Record: `name;category;price;warrantyMonths;stock` (String;String;double;int;int),
separator `;`, UTF-8, no header line and no comments in the CSV.
Metrics: count of valid records, average price, longest warranty,
total stock (sum of `stock`, type long).

## Language
- Code, javadoc, docs, commits, issues, PRs, logs: English.
- Program report and skipped-line messages: Ukrainian, money in "грн".
- Lab reports (`docs/REPORT-lab1.md`, `docs/REPORT-lab2.md`) are written in Ukrainian; do not edit them unless asked.

## Build
- Maven only through the Wrapper: `./mvnw -B clean verify` (Git Bash/Unix), `.\mvnw.cmd -B clean verify` (cmd).
- JUnit 5 (test scope), SpotBugs bound to `verify`, Maven Shade for the executable JAR.
- No runtime dependencies. Package `ua.lpnu.kzp`, entry point `Main`.
- Lifecycle order: compile, test, package, verify.

## Hard rules
- No entity classes (that is lab 2): records are String[] and primitives. Reading, validation,
  calculation and formatting are separate methods or utility classes. Lab 1 rule; Lab 2 allows
  entity classes, see "Lab 2 rules" below.
- Use `Path`, explicit UTF-8, `Locale.ROOT`, `%n`. No hard-coded path separators.
- Validation: trim fields; name and category not blank; numbers must match `-?\d+(\.\d+)?`
  (no exponent, no f/d suffix, no NaN/Infinity); price > 0; warrantyMonths >= 0; stock >= 0;
  int overflow is rejected.
- Every invalid line (including an empty one) yields a message with line number and reason
  and never stops processing. No valid records: report says so, metrics are not computed,
  exit code 1. Argument and I/O errors: exit code 2.
- Report numbers: 2 decimals, locale-independent.
- Logging: English, `java.util.logging`, file `out/app.log`, `--verbose` mirrors to stderr.
  Format: `ISO-8601 time | LEVEL | Class.method | line=N field=X | message`.
  Log startup, arguments, every skipped line with reason, summary, exceptions with stack traces.
- Never disable SpotBugs rules; `@SuppressFBWarnings` only with a justification in the PR.
- Do not commit `target/` or `out/`.
- Javadoc for public classes, `main` and non-trivial methods.
- Docs: concise and factual, no emoji, no filler.

## Workflow
- Tasks live in GitHub Issues. Before starting, read the Issue with `gh issue view <N>` and follow its acceptance criteria.
- One PR covers a small group of related Issues; branch `<type>/<N>-<M>-<slug>`, one logical change per commit. Stay within the scope of the listed Issues.
- Write tests from the Issue acceptance criteria first, then the implementation.
- `clean verify` must be green before finishing a step.
- Conventional Commits (`feat(parser): ...`, `test:`, `ci:`, `build:`, `docs:`, `fix:`),
  one logical change per commit. PR body contains `Closes #N`.
- Show the diff and explain each change briefly. Do not push without confirmation.
- Output style: terse. Do not recap steps already shown or restate file contents; report only command results, diffs and decisions that need my input.

## Lab 2 rules

- Behaviour preservation: report, metrics, error messages, their order and exit codes must
  match the Lab 1 snapshot in `docs/baseline/lab1/`.
- Validation rules live only in `Product` (constructor / `fromCsv`) and the records; no
  duplicated validators.
- Branches are `lab2/<issue-numbers>-<slug>`; one PR per group of Issues, body contains `Closes #N`.
- Tick Issue acceptance criteria and the PR checklist at merge time, only for verified items.
- No push without my explicit "push" message. No merging.
- I run `./mvnw` myself with the `!` prefix when asked.

## Lab 3 rules

Variant 10 "Магазин техніки": inheritance, interfaces, polymorphism. Lab 2 rules above stay in force.

- `Product` becomes an abstract base class (later sealed). Subtypes: `Laptop`, `Peripheral`,
  `OtherProduct` (fallback). Subtypes contain no validation and no field duplication.
- All Lab 2 validation, check order and `ProductValidationException` messages stay in the base.
  The default report output and error messages stay byte-identical to Lab 2
  (`docs/baseline/lab2/`); the golden test must stay green unchanged.
- `ProductKind` enum: label, CSV categories, creator, `fromCategory` never throws,
  unknown maps to `OTHER`. Compare enums with `==`.
- Factories on `Product`: `fromCsv`, `of(...)` replacing the public constructor, `builder()`.
  Old tests are migrated, never deleted or weakened.
- Polymorphic `warrantyCost()`: `Laptop` 3% of price per warranty year plus 100 UAH per started
  year; `Peripheral` 1% per year; `OtherProduct` 2% per year; 0 when `warrantyMonths` is 0.
  No if/switch on subtype for it.
- `equals`/`hashCode`: same class and all five fields (`Double.compare` for price); different
  subtypes are never equal; covered by `HashSet` and `HashMap` tests.
- New optional CLI flag `--warranty-cost` adds a "Гарантійна вартість:" section; without it the
  output is unchanged.
- Star task: sealed hierarchy and exhaustive `switch` with pattern matching and no `default`.
- Version 1.2.0, tag `v1.2.0`, same release and CI-artifact flow as Lab 2.
- Branches `lab3/<issue-numbers>-<slug>`; the prep branch is `chore/lab3-prep`.

## Comment policy

- Javadoc on public API as the lab requires; keep it concise: one sentence of purpose,
  `@param`/`@return`/`@throws` only when not obvious, no HTML lists that mirror the code.
- Inline comments only for non-obvious reasons (why, not what); never restate the code.
- No `// Arrange` / `// Act` / `// Assert` markers in tests.

## Handoff clarity

- Whenever I wait for the user, end the message with exactly one of:
  - `YOUR ACTION: run <exact command> and paste <what>`
  - `YOUR ACTION: reply push`
  - `NOTHING needed from me, I continue`
- State whether the action is required before push or optional.
- No vague phrases such as "next is your step".
