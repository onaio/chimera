(ns chimera.date
  #?(:cljs (:require [cljsjs.moment :as m])))

#?(:cljs (defn format-date
           "If JS doesn't think it's a valid date return the date provided."
           [date]
           (if (or (js/isNaN (new js/Date date))
                   (= (new js/Date date) "Invalid Date"))
             date
             (-> date js/moment (.format "ll")))))
