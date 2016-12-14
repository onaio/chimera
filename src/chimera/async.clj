(ns chimera.async
  (:require [cljs.core.async.macros :refer [go]]))

(defmacro go-inf
  "Like (go (while true ...))"
  [& body]
  `(go (while true ~@body)))

(defmacro chan->body-cljs
  [chan]
  `(-> ~chan cljs.core.async/<! :body chimera.js-interop/json->cljs))
