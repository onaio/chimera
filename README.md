[![example workflow](https://github.com/onaio/chimera/actions/workflows/ci.yml/badge.svg?branch=feature-1)](https://github.com/onaio/chimera/actions/workflows/ci.yml)

# [chimera](https://clojars.org/onaio/chimera)
Clojure Utilities

## Installation

Install via clojars with:

[![Clojars Project](http://clojars.org/onaio/chimera/latest-version.svg)](http://clojars.org/onaio/chimera)


## running JS tests
- Install node version `12.x.x` (you can use nvm)
- Install karma cli
    - `npm install -g karma-cli`
- install npm test dependencies
    - `npm install karma --save-dev`
    - `npm install karma-cljs-test --save-dev`
    - `npm install karma-chrome-launcher --save-dev`
- Run the tests
    - `lein doo chrome-headless test once` or `lein doo chrome-headless test auto`
