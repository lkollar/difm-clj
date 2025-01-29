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

;(defn fetch-all-tracks
;  [mix-show-name username password stop-fn]
;  (loop [counter 1
;         all-episodes []]
;    (let [episodes (audioaddict/get-mixshow-episodes username password mix-show-name counter)]
;      (if (or (empty? episodes) (stop-fn episodes))
;        all-episodes
;        (recur (inc counter) (concat all-episodes episodes))))))

(defn fetch-all-tracks
  [mix-show-name username password]
  (letfn [(fetch-page [page]
            (let [episodes (audioaddict/get-mixshow-episodes username password mix-show-name page)]
              (if (empty? episodes)
                nil
                (lazy-seq (concat episodes (fetch-page (inc page)))))))]
    (fetch-page 1)))

(defn fetch-all-tracks
  ([username password mix-show-name]
   (fetch-all-tracks username password mix-show-name 1))
  ([username password mix-show-name page]
   (let [episodes (audioaddict/get-mixshow-episodes username password mix-show-name page)]
     (if (empty? episodes)
       nil
       (lazy-seq (concat episodes (fetch-all-tracks username password mix-show-name (inc page))))))))


; TODO change fetch-all-tracks to take a seen-tracks, and stop querying when it sees
; any songs from that list. or take a single seen-track
(defn -main
  []
  (let [config (load-config-or-die)
        seen-tracks (io/load-seen-tracks "saved_track_ids.txt")]))

(comment
  (io/parse-config-file "config.edn")
  (try
    (io/parse-config-file "config1.edn")
    (catch Exception e (println "Failed to parse configuration")))

  ;(io/save-seen-tracks "save_tracks.txt" [1 2 3])
  ;(io/load-seen-tracks "saved_track_ids.txt")
  (io/load-seen-tracks "nosuchfile")

  (def config (io/parse-config-file "config.edn"))
  ;(take 2 (fetch-all-tracks (:user config) (:password config) "in-the-mix"))
  (take-while #(not= (:id %) 186007) (fetch-all-tracks (:user config) (:password config) "in-the-mix"))

  (defn contains-id?
    [coll id]
    (some true? (for [x coll
                      :let [_id (:id x)]]
                  (= _id id))))

  (contains-id? fos 186006)

  ;(fetch-all-tracks "in-the-mix" (:user config) (:password config) #(contains-id? % 186006))


  (spit "in-the-mix-shows.edn" (pr-str in-the-mix-shows))
  in-the-mix-shows
  (def fos (take 10 in-the-mix-shows))


  :rcf)
