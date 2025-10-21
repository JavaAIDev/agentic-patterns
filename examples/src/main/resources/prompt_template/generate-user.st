Goal: Generate {count} users. Generated users are also saved as a JSON file to `/tmp`.

Requirements:
- Id should be a version 4 random UUID.
- Name should be using the format "$firstName $lastName".
- Email address should be using the format "$firstName.$lastName@$domain".
- For an address,
  - Country or region must use ISO 3166 alpha-2 code.
  - For province/state/city, they should be generated based on the country or region.
  - Address line can be fake.
  - Zip code should use the format based on the country or region.
- When generating multiple users, choose different countries or regions for those users.
- For a user, generate 1 to 3 addresses. At least one address has the type HOME.