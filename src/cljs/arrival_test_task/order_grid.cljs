(ns arrival-test-task.order-grid
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [arrival-test-task.widgets :as ws]
            [arrival-test-task.fetch :as fetch]
            [garden.core :as garden]))

(defn to-style [css] [:style (garden/css css)])

(defn style [fetching?]
  [:.page ;; {:opacity (if fetching? 0.3 1)}
   ;; moved to :content - for not effect on loader inside page
   
   ;; commons
   [:.input {:outline :none
             :font-size "16px"
             :border-radius "5px"
             :border "2px inset #eee"}]
   [:button {:outline :none
             :font-size "16px"
             :cursor :pointer}]
   [:.icon-btn {:cursor :pointer
                :color :cornflowerblue
                ;; :font-size "2em"
                }]

   [:.content {:margin "0 auto 200px auto"
               :width "80%"
               :opacity (if fetching? 0.3 1)               
               :-webkit-transition "opacity 0.2s" ;; linear" ;; "ease-in"
               }

    [:.header {:display :flex
               :flex-wrap :wrap
               :align-items :flex-end
               :padding "20px 0"}
     [:.title {:display :flex
               :flex-wrap :wrap
               :align-items :baseline
               :padding-bottom "10px"}
      [:.h {:font-size "30pt"
            :font-weight :bold}]
      [:.filter {:display :flex
                 :align-items :baseline}
       [:.label {:padding "0 10px 0 30px"
                 :color "#aaa"}]]]
     [:.actions {:display :flex
                 :align-items :center
                ;; :flex-grow 1
                 :justify-content :flex-end
                 :margin-left "20px"
                 :padding-bottom "10px"}
      [:i {:margin-left "10px"
           :font-size "2em"}]
      [:button {:margin-left "20px"}]]]

    [:.error {:text-align :center
              :color :red
              :font-size "20px"
              :padding-bottom "10px"}]
    
    [:table {:width "100%"
             :border-collapse :collapse}]
    [:th {:color "#aaa"
          :padding "5px 10px"
          :text-align :left}]
    [:td {:font-family :system-ui
          :font-size "16px"
          :vertical-align :baseline
          :padding "5px 10px"}
     [:.row-actions {:padding-top "10px"}
      [:i {:margin-left "10px"}]]]

    [:.item {:padding-top "20px"
             :border-top "1px solid #ddd"}]
    [:.font14 {:font-size "14px"}]
    [:.nowrap {:white-space :nowrap}]
   ; [:.item:hover {:background-color "#f5f5f5"}]
    [:.history {:opacity 0.5}]
    [:.description {:padding-bottom "20px"}]]
   ;;
   ])

(defn loader []
  (let [style [:.loader
               (let [width-height-number 5
                     border-number 1.1
                     width-height (str width-height-number "em")
                     margin-left-top (str "-" (* (+ width-height-number border-number) 0.5) "em")
                     border-trb (str border-number "em solid rgba(214,214,214, 0.2)")
                     border-l (str #_"1.1em" border-number "em solid #d6d6d6")
                     transform "translateZ(0)"
                     animation "load8 1.1s infinite linear"]
                 {:font-size "15px" ;; changing size here!
                  :z-index 100000
                  :opacity "1 !important"
                  :position :fixed
                  :left "50%"
                  :top "50%"
                  :border-radius "50%"
                  :width width-height
                  :height width-height
                  :margin-left margin-left-top
                  :margin-top margin-left-top
                  :border-top border-trb
                  :border-right border-trb
                  :border-bottom border-trb
                  :border-left border-l
                  :-webkit-transform transform
                  :-ms-transform transform
                  :transform transform
                  :-webkit-animation animation
                  :animation animation})]]
    [:div.loader [to-style style]]))


(defn iso-utc-2-iso-local [s]
  (if-not (str/blank? s)
    (let [;; (.toISOString  (js/Date. (js/Date.parse date)))
          ms-utc (js/Date.parse s)
          date (js/Date. ms-utc)
          offset-ms (* 60 1000 (.getTimezoneOffset date))
          ms-local (- (.getTime date) offset-ms)

          date-local (js/Date. ms-local)
          iso-local (.toISOString date-local)]
      (subs iso-local 0 10))))

(defn iso-utc-2-locale-date [s]
  (if-not (str/blank? s) (.toLocaleDateString (js/Date. (js/Date.parse s)))))
(defn iso-utc-2-locale-date-time [s]
  (if-not (str/blank? s) (.toLocaleString (js/Date. (js/Date.parse s)))))

  ; function dateToISOLikeButLocal(date) {const offsetMs = date.getTimezoneOffset() * 60 * 1000;
  ;                                       const msLocal =  date.getTime() - offsetMs;
  ;                                       const dateLocal = new Date(msLocal);
  ;                                       const iso = dateLocal.toISOString();
  ;                                       const isoLocal = iso.slice(0, 19);
  ;                                       return isoLocal;
  ;                                       })


(defn order-form [{:keys [error order]}]
  (let [style [:.form {:background :aliceblue
                       :width "100%"
                       :position :fixed
                       :z-index 1
                       :opacity (if order 1 0)
                       :transform (str "translateY(" (if order 0 -100) "%)")
                      ;  :transform-origin :top
                      ;  :transform (str "scaleY(" (if order 1 0) ")")
                       :-webkit-transition "all 0.2s" ;;" ease-in"
                       }

               ;; commons
               [:.input {:width "100%"}]

               [:.content {:margin "0 auto"
                           :width "80%"}
                [:.h {:font-size "30pt"
                      :font-weight :bold
                      :padding "20px 0 25px"}]
                [:.row {:display :flex
                        :flex-wrap :wrap}
                 [:.text {:flex-grow 1}]
                 [:.non-last {:margin-right "20px"}]]
                [:.label-input {:padding-bottom "10px"}]
                [:.label {:color "#aaa"
                          :margin-bottom "5px"}]
                [:textarea {:min-height "100px"
                            :resize :none}]
                [:.actions {:display :flex
                            :justify-content :center}
                 [:.btn {:font-size "30px"
                         :border-radius "10px"}]
                 [:.save {:background-color :lightgreen
                          :margin-right "20px"}]]
                [:.error {:text-align :center
                          :color :red
                          :font-size "20px"
                          :padding-top "10px"}]
                [:.footer {:padding-bottom "20px"}]]]

        id (:db/id order)]

    [:div.form
     [to-style style]
     [:div.content
      [:div.h (if id (str "Edit order " id) "New order")]
      [:div.row
       [:div.label-input.text.non-last
        [:div.label "Title"]
        [:div (ws/text {:class :input :path [:order :order/title]})]]
       [:div.label-input.text.non-last
        [:div.label "Applicant"]
        [:div (ws/text {:class :input :path [:order :order/applicant]})]]
       [:div.label-input.text.non-last
        [:div.label "Performer"]
        [:div (ws/text {:class :input :path [:order :order/performer]})]]
       [:div.label-input
        [:div.label "Date"]
        [:div (ws/date {:class :input :path [:order :order/date]})]]]
      [:div.label-input
       [:div.label "Description"]
       [:div (ws/textarea {:class :input :path [:order :order/description]})]]
      [:div.actions
       [:button.btn.save
        {:on-click #(rf/dispatch [::save-order])} "Save order"]
       [:button.btn.close
        {:on-click #(rf/dispatch [:set-values-by-paths {:order nil :error nil}])} "Close"]]
      [:div.error (str error)]
      [:div.footer]]]))


(defn order-grid [params]
  (let [{:keys [order-list error fetching? order order-history] :as ps}
        @(rf/subscribe [:get-values-by-paths {:order-list :order-list
                                                    :error :error
                                                    :fetching? :fetching?
                                                    :order :order
                                                    :order-history :order-history}])]
    [:div.page
     [to-style (style fetching?)]

     (when fetching? [loader])
     ;; (when order
     [order-form ps]

     [:div.content
      [:div.header
       [:div.title
        [:div.h "Order list"]
        [:div.filter
         [:div.label "from"]
         [:div (ws/date {:class :input :path [:date-from]
                         :on-change #(rf/dispatch [::load-order-list])})]
         [:div.label "to"]
         [:div (ws/date {:class :input :path [:date-to]
                         :on-change #(rf/dispatch [::load-order-list])})]]]
       [:div.actions
        [:i.material-icons.icon-btn
         {:on-click #(rf/dispatch [:set-values-by-paths {:order {}
                                                         :error nil}])} :add_circle]
        [:i.material-icons.icon-btn
         {:on-click #(rf/dispatch [::load-order-list])} :refresh]]]
      
      [:div.error (str error)]

      ;; fetching? [:div.loader "Loading"]
      ;; (empty? order-list) [:div "Nothing to show"]
      [:table
       (reduce
        (fn [acc ;; [id title description applicant performer date]
             {id :db/id
              title :order/title
              description :order/description
              applicant :order/applicant
              performer :order/performer
              date :order/date}]
          (-> acc
              (conj
               [:tr {:key (str id "_1")}
                [:td.item {:row-span 2}
                 id
                 [:div.row-actions
                  [:i.material-icons.icon-btn
                   {:on-click #(rf/dispatch [::fetch-order id])} :edit]
                  [:i.material-icons.icon-btn
                   {:on-click #(rf/dispatch [::fetch-order-history id])} :history]]]
                [:td.item title] [:td.item applicant] [:td.item performer]
                [:td.item.date (iso-utc-2-locale-date date)]]
               [:tr.bottom {:key (str id "_2")}
                [:td.font14.description {:col-span 4} description]])
              ;;
              (into (mapcat (fn [[tr-date x]]
                              [[:tr.history
                            ;; {:key (str id "_1")}
                                [:td.font14.nowrap (iso-utc-2-locale-date-time tr-date)]
                                [:td (:order/title x)]
                                [:td (:order/applicant x)]
                                [:td (:order/performer x)]
                                [:td.date (iso-utc-2-locale-date (:order/date x))]]
                               [:tr.history
                               ;; {:key (str id "_2")}
                                [:td]
                                [:td.font14.description {:col-span 4} (:order/description x)]]])
                            (get order-history id)))
              ;;
              ))
        [:tbody
         [:tr
          [:th "ID"] [:th "Title"] [:th "Applicant"] [:th "Performer"] [:th.date "Date"]]]
        ;; (sort-by first > order-list)
        (vals order-list)
        ;;
        )]]]))


(def backend-url "http://localhost:3000")


(rf/reg-event-fx
 ::load-order-list
 (fn [{db :db} _]
   (when-not (:fetching? db)
     (let [{:keys [date-from date-to]} db]
       {:fetch-promise
        (-> (fetch/fetch-promise {:uri (str backend-url "/order")
                                  :params (cond-> nil
                                            (not (str/blank? date-from)) (assoc :date-from date-from)
                                            (not (str/blank? date-to)) (assoc :date-to date-to))})
            (.then (fn [x] (rf/dispatch [:set-values-by-paths {:order-list (:data x)}]))))}))))

(rf/reg-event-fx
 ::save-order
 (fn [{db :db} _]
   (when-not (:fetching? db)
     {:fetch-promise
      (-> (fetch/fetch-promise {:uri (str backend-url "/order-save")
                                :method "POST"
                                :body (:order db)})
          (.then (fn [order]
                   (js/Promise.all
                    [order
                     (let [id (get-in order [:data :db/id])]
                       (when (get-in db [:order-history id])
                         (fetch/fetch-promise {:uri (str backend-url "/order-history/" id)})))])))
          (.then (fn [[order history]]
                   (let [id (get-in order [:data :db/id])]
                     (rf/dispatch
                      [:set-values-by-paths
                       (cond-> {[:order-list (keyword (str id))]
                                (update (:data order) :order/date iso-utc-2-iso-local)
                                :order nil}
                         history (assoc [:order-history id] (:data history)))])))))})))

(rf/reg-event-fx
 ::fetch-order
 (fn [{db :db} [_ id]]
   (when-not (:fetching? db)
     {:fetch-promise
      (-> (fetch/fetch-promise {:uri (str backend-url "/order/" id)})
          (.then (fn [x] (rf/dispatch
                          [:set-values-by-paths
                           {:order (update (:data x) :order/date iso-utc-2-iso-local)}]))))})))

(rf/reg-event-fx
 ::fetch-order-history
 (fn [{db :db} [_ id]]
   (if (get-in db [:order-history id])
     (rf/dispatch [:set-values-by-paths {[:order-history id] nil}])
     (when-not (:fetching? db)
       {:fetch-promise
        (-> (fetch/fetch-promise {:uri (str backend-url "/order-history/" id)})
            (.then (fn [x] (rf/dispatch [:set-values-by-paths
                                               {[:order-history id] (:data x)}]))))}))))

(def routes {:order-grid (fn [params]
                           (rf/dispatch [::load-order-list params])
                           [order-grid (:query-params params)])})
