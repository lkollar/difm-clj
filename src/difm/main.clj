(ns difm.main
  (:require [clojure.string :as str]
            [difm.audioaddict :as audioaddict]
            [difm.id3tagger :as id3tagger]
            [difm.io :as io])
  (:import (java.io FileNotFoundException)
           (java.util.logging Level Logger))
  (:gen-class))

(def ^:private seen-tracks-filename "saved_track_ids.txt")

; TODO Work out CWD and always try to read config files from there
; TODO validate config schema
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
        (if (or (empty? episodes) (contains-seen-track episodes))
          all-episodes
          (recur (inc counter) (concat all-episodes episodes)))))))


(defn get-track-info
  [episode]
  (let [first-track (first (:tracks episode))
        artist (:name (first (:artists first-track)))
        image-url (:default (:images first-track))
        title (:title first-track)
        file-name (str title ".mp4")
        image-url (str/replace image-url #"^//" "https://")]

    {:id        (:id first-track)
     :slug      (Integer/parseInt (:slug episode))
     ; In some cases DJ is mistyped as DD
     :artist    (str/replace artist #"^DD" "DJ")
     :image-url (str/replace image-url #"\{.*" "")
     :show-name (:name (:show episode))
     :title     title
     :file-name file-name}))

(defn get-track-url
  [username password track-id]
  (let [track-info (audioaddict/get-track username password track-id)
        asset (first (:assets (:content track-info)))
        url (:url asset)]
    (str/replace url #"^//" "https://")))

(defn update-id3-tags
  [track-info file]
  (let [cover-art (audioaddict/download-cover (:image-url track-info))]
    (do
      (id3tagger/set-mp4-tags file
                              :title (:title track-info)
                              :track (str (:slug track-info))
                              :artist (:artist track-info)
                              :album (:show-name track-info))
      (id3tagger/set-mp4-cover file cover-art))))

(defn save-track
  [username password episode]
  (let [track-info (get-track-info episode)
        track-url (get-track-url username password (:id track-info))
        file-name (:file-name track-info)]
    (do
      (println (str "Downloading " file-name))
      (audioaddict/download-track track-url file-name)
      (update-id3-tags track-info file-name))))


(defn -main
  [& args]
  (let [config (load-config-or-die)
        username (:user config)
        password (:password config)
        seen-tracks (io/load-seen-tracks seen-tracks-filename)
        new-tracks (fetch-all-tracks "in-the-mix" (:user config) (:password config) seen-tracks)
        new-track-ids (map #(-> %
                                :tracks
                                first
                                :id)
                           new-tracks)]
    (doseq [track new-tracks]
      ; Silence jaudiotagger spammy logs
      (.setLevel (Logger/getLogger "org.jaudiotagger") Level/OFF)
      (println (str "Processing track " (:slug track)))
      (save-track username password track)
      (io/save-seen-tracks seen-tracks-filename (concat seen-tracks new-track-ids)))))

; Parse configuration
(comment
  (-main)
  (io/parse-config-file "config.edn")

  ; error handling
  (try
    (io/parse-config-file "config1.edn")
    (catch Exception e (println "Failed to parse configuration")))

  (io/load-seen-tracks "saved_track_ids.txt")
  :rcf)

; Load canned show metadata
(comment
  (def config (io/parse-config-file "config.edn"))
  ;(spit "in-the-mix-shows.edn" (pr-str in-the-mix-shows))
  (def in-the-mix-shows (clojure.edn/read-string (slurp "in-the-mix-shows.edn")))
  (def single-track (first in-the-mix-shows))
  (def track-info (get-track-info single-track))
  (:show-name track-info)
  (def track-id (:id track-info))
  (def track (audioaddict/get-track (:user config) (:password config) track-id))
  (save-track (:user config) (:password config) single-track)
  (get-track-url (:user config) (:password config) track-id)

  :rcf)

; MP4 tagging
(comment
  (id3tagger/set-mp4-tags "In The Mix Episode 422 (31 October 2024).mp4" :album "In the Mix")

  :rcf)