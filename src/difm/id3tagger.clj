(ns difm.id3tagger
  (:require [clojure.string :as string])
  (:import (org.jaudiotagger.audio AudioFileIO)
           (org.jaudiotagger.tag FieldKey)
           (org.jaudiotagger.tag.datatype Artwork)))

(defn- ->constant [k]
  (.get (.getField FieldKey k) nil))

(defn- constantify [k]
  (string/replace (string/upper-case (name k)) #"-" "_"))

(defn set-mp4-tags
  [file-path & tags]
  (let [audio-file (AudioFileIO/read (java.io.File. file-path))
        tag (.getTag audio-file)]
    (doseq [[tag-name tag-value] (partition 2 tags)]
      (let [field-name (->constant (or (and (keyword? tag-name) (constantify tag-name)) tag-name))]
        (.setField tag field-name tag-value)))
    (.commit audio-file)))

(defn set-mp4-cover
  [file-path cover-bytes]
  (let [audio-file (AudioFileIO/read (java.io.File. file-path))
        tag (.getTag audio-file)
        artwork (Artwork.)
        _ (.setBinaryData artwork cover-bytes)]
    (.setField tag artwork)
    (.commit audio-file)))