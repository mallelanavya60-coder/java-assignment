# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
I would gradually standardize database access where it improves maintainability, but I would avoid refactoring everything simply to make the code look uniform.

Currently, the code uses different approaches: Store and Product use Panache active-record-style persistence, while Warehouse follows a more layered architecture with domain models, ports, use cases, and a database repository adapter. Panache is concise and fits straightforward CRUD operations well. The repository/port approach adds some boilerplate, but it separates business logic from persistence details, makes unit testing easier, and allows the underlying storage implementation to change with less impact on the domain.

I would preserve Panache for simple entities unless their business logic becomes more complex. For Warehouse, I would continue using the existing repository and use-case structure because the domain includes several business rules, such as location capacity, warehouse-count limits, stock validation, replacement, and archiving. I would also make transaction boundaries explicit for operations involving multiple database changes, consistently handle active versus archived records, and enforce suitable invariants through database constraints where possible.

For Store synchronization, I would ensure that calls to the legacy system happen only after the database transaction commits. Transaction synchronization is a reasonable solution for this requirement. However, if reliable delivery across application crashes is required, I would introduce a transactional outbox so that the database change and an event describing it are persisted atomically. A background worker could then deliver the event with retries and appropriate idempotency handling.

Overall, I would favor incremental, domain-driven refactoring based on complexity and risk rather than imposing one persistence pattern everywhere.

```
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
Both approaches have advantages, and I would choose based on the API's consumers, stability requirements, and expected evolution.

Generating API interfaces and models from an OpenAPI specification supports a contract-first development process. The API contract can be reviewed before implementation, documentation is explicit, and generated types help keep the implementation aligned with the agreed request and response structures. This is particularly valuable when multiple teams or external clients depend on the API. The disadvantages are additional build and generator configuration, generated code that can be less convenient to customize, and the need to manage specification changes and generated files carefully.

Hand-written endpoints provide more direct control and can be faster to develop for small internal APIs. They avoid some code-generation complexity and allow developers to shape handlers according to the application's needs. However, API documentation, validation, response formats, and status codes can become inconsistent over time if they are not maintained and tested carefully.

For this project, I would use OpenAPI as the source of truth for all externally consumed endpoints, including Warehouse, Product, and Store. I would generate the API contracts and models while keeping business rules in application services or use cases rather than placing them in generated classes. This would give the project a consistent contract and make it easier to review API changes. I would also add contract and integration tests to verify that the implementation follows the specification, including validation errors and HTTP status codes.

For a small internal endpoint with limited consumers, hand-written code can still be a reasonable choice. The important point is to make that choice deliberately and maintain a clear, testable contract.

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
I would prioritize tests according to business risk, the likelihood of regression, and the impact of failure, rather than aiming for a coverage percentage alone. I would build a fast unit-test suite first, followed by database integration tests and API-level tests for the most important workflows.

First, I would unit-test the Warehouse use cases. These tests should cover successful creation, replacement, and archiving, along with the main validation rules: duplicate business-unit codes, unknown locations, maximum warehouse counts per location, aggregate location capacity, stock exceeding capacity, replacement stock matching, and whether the replacement warehouse can accommodate the existing stock. I would use fake repositories and location resolvers where appropriate so that these tests remain fast and deterministic.

Second, I would add database integration tests to verify behavior that unit tests cannot fully establish. These would cover persistence, active versus archived warehouse queries, replacement history, and transaction behavior. I would also test the Store synchronization requirement to verify that the legacy gateway is not called if the database transaction rolls back and is called only after a successful commit.

Third, I would extend the REST endpoint tests to cover request validation, response payloads, missing resources, conflicts, and expected HTTP status codes. For example, invalid requests should return an appropriate client error, missing resources should return a not-found response, and business-rule conflicts should be represented consistently. Where practical, I would test the generated Warehouse API contract as well.

For the optional Product, Store, and Warehouse associations, I would add boundary tests for each constraint: no more than two warehouses per product per store, three warehouses per store, and five product types per warehouse. I would test both the maximum permitted value and the first rejected value.

To keep coverage effective over time, I would run unit tests on every change and integration tests in CI, require regression tests for bug fixes, and review coverage reports alongside the actual business rules and failure paths. I would use coverage to identify gaps, not as the sole measure of quality. Periodically reviewing redundant or slow tests and keeping test data isolated would help maintain a reliable, efficient test suite.

```