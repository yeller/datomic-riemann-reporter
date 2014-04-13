(ns datomic-riemann-reporter
  (:require [riemann.client :as riemann]
            [environ.core :as environ]))

(def *client* nil)

(defn client []
  (if *client*
    *client*
    (if (and (environ/env :riemann-host) (environ/env :riemann-port))
      (let [initialized (riemann/tcp-client :host (environ/env :riemann-host) :port (environ/env :riemann-port))]
        (alter-var-root #'*client* (constantly initialized))
        initialized))))

(defn send-event [event]
  (if-let [actual-client (client)]
    (riemann/async-send-event
      actual-client
      (merge event
             {:tags ["datomic"]}))
    (prn (assoc event
                :riemann-reporter-error :no-client
                :riemann-reporter (client)
                :riemann-host (environ/env :riemann-host)
                :riemann-port (environ/env :riemann-port)))))

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
