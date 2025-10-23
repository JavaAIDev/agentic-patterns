# Observation with Langfuse

Copy `langfuse.env.example` to `langfuse.env`.

Generate Langfuse HTTP Basic auth header using `$(echo -n 'PUBLIC_KEY:SECRET_KEY' | base64)`, then set to the value of `LANGFUSE_AUTH_STRING`.