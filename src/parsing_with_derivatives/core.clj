(ns parsing-with-derivatives.core)

(defmulti match-one (fn [{:keys [::type]} _] type))
(defmulti matched? (fn [{:keys [::type]}] type))
(defmulti normalize (fn [{:keys [::type]}] type))
(defmulti pattern->regex (fn [{:keys [::type]}] type))
(defmulti subpattern->regex (fn [{:keys [::type]}] type))
(defmethod subpattern->regex :default [subpattern] (pattern->regex subpattern))

(defn match [regex string] (matched? (reduce match-one regex string)))

;;; Failure Pattern
(defn failure-pattern [] {::type :failure} (throw (ex-info "Unimplemented" {::type :failure})))


;;; Empty Pattern
(defn empty-pattern [] {::type :empty} (throw (ex-info "Unimplemented" {::type :empty})))


;;; Any Character Pattern
(defn any-character-pattern [] {::type :any-character} (throw (ex-info "Unimplemented" {::type :any-character})))


;;; Character Pattern
(defn character-pattern [] {::type :character} (throw (ex-info "Unimplemented" {::type :character})))


;;; Alternation Pattern
(defn alternation-pattern [& patterns] {::type :alternation} (throw (ex-info "Unimplemented" {::type :alternation})))


;;; Sequence Pattern
(defn sequence-pattern [& patterns] {::type :sequence} (throw (ex-info "Unimplemented" {::type :sequence})))


;;; Repetition Pattern
(defn repetition-pattern [pattern] {::type :repetition} (throw (ex-info "Unimplemented" {::type :repetition})))


;;; Character Set Pattern
(defn character-set-pattern [& ranges]
  {::type :character-set}
  (throw (ex-info "Unimplemented" {::type :character-set})))


;;; Negated Character Set Pattern
(defn negated-character-set-pattern [& ranges]
  {::type :negated-character-set}
  (throw (ex-info "Unimplemented" {::type :negated-character-set})))


;;; Capture Pattern
(defn capture-pattern [id] {::type :capture} (throw (ex-info "Unimplemented" {::type :capture})))


;;; regex->pattern
(defmulti read-one {:argslists '([[regex-string patterns]])} (fn [[[first-character & _] _]] first-character))
;;; TODO: Literal
;;; TODO: .
;;; TODO: [...]
;;; TODO: [^...]
;;; TODO: ^
;;; TODO: $
;;; TODO: (...)
;;; TODO: *
;;; TODO: +
;;; TODO: ?
;;; TODO: |
;;; TODO: {m,n}
;;; TODO: {m}
;;; TODO: {m,}
;;; TODO: {,n}
;;; TODO: Character Classes
;;; TODO: Capturing Groups
;;; TODO: Non-capturing Group

(defn regex->pattern [regex]
  (apply sequence-pattern
         (some (fn [[regex patterns]] (and (empty? regex) patterns)) (iterate read-one [(str regex) []]))))
