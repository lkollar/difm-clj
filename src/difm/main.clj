(ns difm.main
  (:require [difm.audioaddict :as audioaddict]
            [difm.io :as io])
  (:import (java.io FileNotFoundException)))

; TODO Work out CWD and always try to read config files from there

(defn load-config-or-die
  []
  (try
    (let [config (io/parse-config-file "config.edn")] config)
    (catch FileNotFoundException _ (do
                                     (println "Could not read 'config.edn'")
                                     (System/exit 1)))))


(defn fetch-all-tracks
  [mix-show-name username password seen-tracks]
  (letfn [(get-track-id [track-response] (:id (first (:tracks track-response))))
          (contains-seen-track [episodes] (some (set (map get-track-id episodes)) seen-tracks))]
    (loop [counter 1
           all-episodes []]
      (let [episodes (audioaddict/get-mixshow-episodes username password mix-show-name counter)]
        (do
          (let [track-ids (map get-track-id episodes)]
            (println "Got track ids: " track-ids)
            (println "Seen track ids: " seen-tracks)))
        (if (or (empty? episodes) (contains-seen-track episodes))
          all-episodes
          (recur (inc counter) (concat all-episodes episodes)))))))


(defn -main
  []
  (let [config (load-config-or-die)
        seen-tracks (io/load-seen-tracks "saved_track_ids.txt")
        new-tracks (fetch-all-tracks "in-the-mix" (:user config) (:password config) seen-tracks)]
    (println (pr-str "Found new tracks: " new-tracks))))

(comment
  (-main)
  (io/parse-config-file "config.edn")
  (try
    (io/parse-config-file "config1.edn")
    (catch Exception e (println "Failed to parse configuration")))

  ;(io/load-seen-tracks "saved_track_ids.txt")
  (io/load-seen-tracks "nosuchfile")

  (def config (io/parse-config-file "config.edn"))

  (spit "in-the-mix-shows.edn" (pr-str in-the-mix-shows))
  (def in-the-mix-shows (clojure.edn/read-string (slurp "in-the-mix-shows.edn")))
  in-the-mix-shows


  :rcf)
