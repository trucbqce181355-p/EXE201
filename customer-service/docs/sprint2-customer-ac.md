# Sprint 2 Customer Service Assumptions

This document captures the working assumptions used to implement the first
three customer stories in the absence of a formal Sprint 2 AC document.
The structure follows the same style used in Sprint 1.

## Story 20: Create Customer Account

### Happy Path

- Authenticated back-office caller creates a customer profile linked 1-1 to `authUserId`.
- Required fields: `authUserId`, `fullName`, `email`, `phone`.
- Optional field: `address`.
- Default values:
  - `status = ACTIVE`
  - `segment = NEW`
  - `loyaltyTier = BRONZE`
  - `loyaltyPoints = 0`
- Response returns `201 Created` with the customer profile in the standard API envelope.

### Validation

- `authUserId` is required.
- `fullName` max length is 100 characters.
- `email` must be valid and max length is 150 characters.
- `phone` must match `+?[0-9]{9,15}`.
- `address` max length is 255 characters.

### Error Handling

- Duplicate `authUserId` returns `409 Conflict`.
- Duplicate `email` returns `409 Conflict`.
- Invalid payload returns `400 Bad Request`.

### Security

- Requires a valid JWT.
- Intended actor is `Admin / Staff / Franchise Manager`.
- Role-specific authorization should be tightened once Sprint 2 role names are finalized.

## Story 19: View Customer Profile

### Happy Path

- `GET /api/customers/{id}` returns a customer profile by internal customer id for back-office users.

### Error Handling

- Missing or invalid JWT returns `401 Unauthorized`.
- Missing customer profile returns `404 Not Found`.

### Security

- Requires a valid JWT.
- Intended actor is `Admin / Staff / Franchise Manager`.

## Story 21: Update Customer Profile

### Happy Path

- `PUT /api/customers/{id}` updates a customer profile by internal customer id for back-office use.
- Allowed fields: `fullName`, `email`, `phone`, `address`.

### Validation

- Same field validation rules as Story 20.

### Error Handling

- Duplicate `email` returns `409 Conflict`.
- Invalid payload returns `400 Bad Request`.
- Missing customer profile returns `404 Not Found`.

### Security

- Requires a valid JWT.
- Intended actor is `Admin / Staff / Franchise Manager`.
- Authorization rules for `/api/customers/{id}` should be tightened once Sprint 2 role requirements are finalized.
