(ns test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [chimera.core-test]
   [chimera.date-test]
   [chimera.js-interop-test]
   [chimera.metrics-test]
   [chimera.seq-test]
   [chimera.string-test]))

(enable-console-print!)

(doo-tests 'chimera.core-test
           'chimera.date-test
           'chimera.metrics-test
           'chimera.js-interop-test
           'chimera.seq-test
           'chimera.string-test)
