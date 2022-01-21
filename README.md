[![Build Status](https://travis-ci.org/onaio/chimera.svg?branch=master)](https://travis-ci.org/onaio/chimera)

# chimera
Clojure Utilities


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
