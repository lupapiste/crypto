(defproject lupapiste/crypto "0.1.3"
  :description "Crypto with bouncycastle"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.bouncycastle/bcprov-jdk18on "1.77"]]
  :profiles {:dev {:dependencies [[midje "1.10.10" :exclusions [org.clojure/clojure]]]
                   :plugins      [[lein-midje "3.2.2"]]}}
  :min-lein-version "2.5.0")
