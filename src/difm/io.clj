(ns difm.io
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn parse-config-file
  [config-path]
  (let [config-file (slurp config-path)
        parsed-edn (edn/read-string config-file)]
    parsed-edn))

(defn load-seen-tracks
  [file-path]
  (try
    (with-open [rdr (io/reader file-path)]
      (doall (map #(Integer/parseInt %) (line-seq rdr))))
    (catch Exception _ [])))

(defn save-seen-tracks
  [file-path track-ids]
  (with-open [w (io/writer file-path)]
    (doseq [id track-ids]
      (.write w (str id "\n")))))
