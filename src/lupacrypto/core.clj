(ns lupacrypto.core
  (:require [crypto.random :as cr])
  (:import [org.bouncycastle.crypto BufferedBlockCipher]
           [org.bouncycastle.crypto.engines RijndaelEngine AESEngine]
           [org.bouncycastle.crypto.modes CBCBlockCipher]
           [org.bouncycastle.crypto.paddings PaddedBufferedBlockCipher ZeroBytePadding PKCS7Padding]
           [org.bouncycastle.crypto.params KeyParameter ParametersWithIV]
           [org.apache.commons.codec.binary Base64]))

(defn make-iv
  "Random bytes for initialization vector. Default is 32"
  ([] (make-iv 32))
  ([bytes]
   (cr/bytes bytes)))

(defn make-iv-128 []
  (make-iv 16))

(def ^:private ciphers
  {:rijndael (fn [encrypt? crypto-key crypto-iv]
               (doto (-> (RijndaelEngine. 256)
                         (CBCBlockCipher.)
                         (PaddedBufferedBlockCipher. (ZeroBytePadding.)))
                 (.init encrypt? (ParametersWithIV. (KeyParameter. (into-array Byte/TYPE crypto-key))
                                                    (into-array Byte/TYPE crypto-iv)))))

   :aes      (fn [encrypt? crypto-key crypto-iv]
               (doto (-> (AESEngine.)
                         (CBCBlockCipher.)
                         (PaddedBufferedBlockCipher. (PKCS7Padding.)))
                 (.init encrypt? (ParametersWithIV. (KeyParameter. (into-array Byte/TYPE crypto-key))
                                                    (into-array Byte/TYPE crypto-iv)))))})

(defn- crypt ^bytes [^BufferedBlockCipher cipher ^bytes data]
  (let [in-size  (alength data)
        out-size (.getOutputSize cipher in-size)
        out      (byte-array out-size)
        out-len  (.processBytes cipher data 0 in-size out 0)
        out-len  (+ out-len (.doFinal cipher out out-len))]
    (if (< out-len out-size)
      (byte-array out-len out)
      out)))

(defn encrypt
  [crypto-key crypto-iv cipher data]
  {:pre [(contains? ciphers cipher)]}
  (crypt ((cipher ciphers) true crypto-key crypto-iv) data))

(defn decrypt
  [crypto-key crypto-iv cipher data]
  {:pre [(contains? ciphers cipher)]}
  (crypt ((cipher ciphers) false crypto-key crypto-iv) data))

(defn str->bytes ^bytes [^String s] (.getBytes s "UTF-8"))
(defn bytes->str ^String [^bytes b] (String. b "UTF-8"))

(defn base64-encode [^bytes data] (Base64/encodeBase64 data))
(defn base64-decode [^bytes data] (Base64/decodeBase64 data))

(defn url-encode [^String s] (java.net.URLEncoder/encode s "UTF-8"))

(defn encrypt-aes-string
  "Encrypt string with AES using given base64 passowrd string and IV byte array"
  [s crypto-key-s crypto-iv]
  (let [crypto-key (-> crypto-key-s str->bytes base64-decode)]
    (->> s
         str->bytes
         (encrypt crypto-key crypto-iv :aes)
         base64-encode
         bytes->str)))

(defn decrypt-aes-string
  "Arguments are expected to be base64 encoded"
  [s crypto-key-s crypto-iv-s]
  (let [crypto-key (-> crypto-key-s str->bytes base64-decode)
        crypto-iv  (-> crypto-iv-s str->bytes base64-decode)]
    (->> s
         str->bytes
         base64-decode
         (decrypt crypto-key crypto-iv :aes)
         bytes->str)))
