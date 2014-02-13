(ns datomic-riemann-reporter
  (:require [riemann.client :as riemann]
            [environ.core :as environ]))

(def *client* nil)

(defn client []
  (if *client*
    *client*
    (let [initialized (riemann/tcp-client :host (environ/env :riemann-host) :port (environ/env :riemann-port))]
      (alter-var-root #'*client* (constantly initialized))
      initialized)))

(defn send-event [event]
  (riemann/send-event
    (client)
    (merge event
           {:tags ["datomic"]})))

(defn report-datomic-metrics-to-riemann [metrics]
  (doseq [[metric-name value] metrics]
    (if (map? value)
      (doseq [[sub-metric-name sub-metric-value] value]
        (send-event
          {:service (str "datomic " (name metric-name) " " (name sub-metric-name))
           :metric sub-metric-value
           :state "ok"
           :ttl 60}))
      (send-event
        {:service (str "datomic " (name metric-name))
         :metric value
         :state "ok"
         :ttl 60}))))
