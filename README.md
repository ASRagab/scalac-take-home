# Github API Take Home

## Prerequisites

To get contributors from even medium sized orgs, you'll need a github access token and set 
and environment variable `GH_TOKEN` with the value.

[Github Docs](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)


## Running

```bash
> export GH_TOKEN=<your-github-token>; sbt run
```


## Testing

```bash
> sbt test
```

## Development Server

Thanks to sbt-revolver, we can run the application in development mode
will watch for source changes and restart the server

```bash
> export GH_TOKEN=<your-github-token>; sbt "~reStart"
```

## Choices and Regrets

1. I wanted to try `zio-http`, mostly for selfish reasons. I was eager to test a new framework and use it in anger.
- Unfortunately it is a little immature still, some import features are missing
  1. Caching Middleware
  2. Testing Harness is barebones and doesn't play well with other ZIO isms

2. `Sttp` is an outstanding library, it is sometimes quite opinionated. 

3. The rate-limiting is quite a challenge to workaround. 

4. Not enough testing, this is mostly my fault, I had less time than I thought.

5. Unfortunately, had some timeouts and other errors on very large organizations, like Google.

Some things I liked about my implementation, the services are well composed I think
and I effectively used the ZLayer mechanisms to do so. Additionally,
I think using the link headers to gather pages and collect them in parallel, 
makes the performance at least plausible for medium sized orgs. 