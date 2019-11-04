(defproject lupapiste/crypto "0.1.1"
 :description "Crypto with bouncycastle"
 :dependencies [[org.clojure/clojure "1.10.1"]
                [commons-codec "1.10"]
                [crypto-random "1.2.0" :exclusions [commons-codec]]
                [org.bouncycastle/bcprov-jdk15on "1.64"]]
 :profiles {:dev {:dependencies [[midje "1.9.9" :exclusions [org.clojure/clojure commons-codec]]]
                  :plugins [[lein-midje "3.2.1"]]}}
 :min-lein-version "2.5.0")
