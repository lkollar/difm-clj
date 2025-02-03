(ns test_difm
  (:require [clojure.test :as t :refer [deftest is]]
            [difm.io :as io])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(defn with-temp-file [f]
  (let [temp-file (Files/createTempFile "test" ".tmp" (into-array FileAttribute []))]
    (try
      (f (.toFile temp-file))
      (finally
        (io/delete-file (.toFile temp-file))))))

(deftest write-and-read-seen-tracks
  (with-temp-file
    (fn [temp-file]
      (let [_ (io/save-seen-tracks temp-file [1 2 3])
            loaded-tracks (io/load-seen-tracks temp-file)]
        (is (= loaded-tracks [1 2 3]))))))

(deftest read-seen-tracks-nonexistent
  (let [result (io/load-seen-tracks "nosuchpath")]
    (is (= result []))))

(t/run-tests 'test_difm)
