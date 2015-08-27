(ns shurly.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [shurly.data :as data]
            [environ.core :refer [env]]))

(defroutes routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (slurp (io/resource "index.html"))})
  (GET "/:slug" [slug]
       {:status 301
        :headers {"Location" (data/request-redirect! slug)}})
  (POST "/shorten" [slug target]
        (if (data/store-slug! slug target)
          {:status 200}
          {:status 404}))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(def app (-> routes
             params/wrap-params))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
