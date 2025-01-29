(ns difm.audioaddict
  (:require [clj-http.lite.client :as client]
            [clojure.data.json :as json]))

(def ^:private endpoint-auth "https://api.audioaddict.com/v1/di/members/authenticate")

(defn authenticate
  [user password]
  (let [response (client/post endpoint-auth
                              {:headers               {"Authorization" "Basic c3RyZWFtczpkaXJhZGlv"} ; b64 encoded 'streams:diradio'
                               :body                  (json/write-str {:username user :password password})
                               :content-type          :json
                               :throw-entire-message? true})
        response-body (:body response)]
    (json/read-str response-body :key-fn keyword)))

(defn ^:private send-request
  [username password url & extra-query-params]
  (let [auth-query-params {"username" username "password" password}
        query-params (into auth-query-params extra-query-params)
        response (client/get url {:query-params query-params})]
    (json/read-str (:body response) :key-fn keyword)))

(defn get-favourites
  [username password]
  (send-request username password "https://api.audioaddict.com/v1/di/members/1/favorites/channels"))

(defn get-channels
  [username password]
  (send-request username password "https://api.audioaddict.com/v1/di/channels"))

(defn get-channel-details
  [username password channel-id]
  (send-request username password (str "https://api.audioaddict.com/v1/di/channels/" channel-id)))

(defn get-channel-events
  [username password channel-id]
  (send-request username password (str "https://api.audioaddict.com/v1/di/events/channel/" channel-id)))

(defn get-track
  [username password track-id]
  (send-request username password (str "https://api.audioaddict.com/v1/di/tracks/" track-id)))

(defn get-mixshow-episodes
  "Fetch episodes for the specified mix show.

  To fetch all data, increment page until the response is empty."
  [username password mixshow-name page]
  (send-request username password
                (str "https://api.audioaddict.com/v1/di/shows/" mixshow-name "/episodes")
                {"per_page" 10 "page" page}))

(comment
  (def user "<redacted>")
  (def password "<redacted>")
  (def auth-resp (authenticate user password))
  (def auth-info {:user     user
                  :password password})
  (:api_key auth-resp)
  (:listen_key auth-resp)

  (get-favourites user password)
  (get-channels user password)
  (get-channel-details user password 13)
  (get-channel-events user password 13)
  (get-mixshow-episodes user password "in-the-mix" 1)
  (get-track user password 292297)

  (let [x (range (+ 1 10))
        y (+ 1 10)]
    (doseq [x (println x)]))

  :rcf)
