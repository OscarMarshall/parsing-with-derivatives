(ns parsing-with-derivatives.core-test
  (:require [parsing-with-derivatives.core :as sut]
            [clojure.test :as t]))

(t/deftest failure-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/failure-pattern) \a))))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/failure-pattern)))))

  (t/testing "normalize"
    (t/is (= (sut/failure-pattern) (sut/normalize (sut/failure-pattern)))))

  (t/testing "pattern->regex"
    (t/is (nil? (sut/pattern->regex (sut/failure-pattern))))))

(t/deftest empty-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/empty-pattern) \a))))

  (t/testing "matched?"
    (t/is (true? (sut/matched? (sut/empty-pattern)))))

  (t/testing "normalize"
    (t/is (= (sut/empty-pattern) (sut/normalize (sut/empty-pattern)))))

  (t/testing "pattern->regex"
    (t/is (= "" (str (sut/pattern->regex (sut/empty-pattern)))))))

(t/deftest any-character-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/any-character-pattern) \a))))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/any-character-pattern)))))

  (t/testing "normalize"
    (t/is (= (sut/any-character-pattern) (sut/normalize (sut/any-character-pattern)))))

  (t/testing "pattern->regex"
    (t/is (= "." (str (sut/pattern->regex (sut/any-character-pattern)))))))

(t/deftest character-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/character-pattern \a) \b)))
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/character-pattern \a) \a))))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/character-pattern \a)))))

  (t/testing "normalize"
    (t/is (= (sut/character-pattern \a) (sut/normalize (sut/character-pattern \a)))))

  (t/testing "pattern->regex"
    (t/is (= "a" (str (sut/pattern->regex (sut/character-pattern \a)))))))

(t/deftest alternation-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern)
             (sut/match-one (sut/alternation-pattern (sut/character-pattern \a) (sut/character-pattern \b))
                            \c))
          "#\"a|b\" << c => nil")
    (t/is (= (sut/empty-pattern)
             (sut/match-one (sut/alternation-pattern (sut/character-pattern \a) (sut/character-pattern \b))
                            \a))
          "#\"a|b\" << a => #\"\"")
    (t/is (= (sut/empty-pattern)
             (sut/match-one (sut/alternation-pattern (sut/character-pattern \a) (sut/character-pattern \b))
                            \b))
          "#\"a|b\" << b => #\"\"")
    (t/is (= (sut/alternation-pattern (sut/character-pattern \a) (sut/character-pattern \b))
             (sut/match-one (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \a)
                                                                           (sut/character-pattern \a))
                                                     (sut/sequence-pattern (sut/any-character-pattern)
                                                                           (sut/character-pattern \b)))
                            \a))
          "#\"aa|.b\" << a => #\"a|b\""))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/alternation-pattern (sut/any-character-pattern) (sut/any-character-pattern)))))
    (t/is (true? (sut/matched? (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern)))))
    (t/is (true? (sut/matched? (sut/alternation-pattern (sut/any-character-pattern) (sut/empty-pattern))))))

  (t/testing "normalize"
    (t/is (= (sut/alternation-pattern (sut/empty-pattern) (sut/character-pattern \a))
             (sut/normalize (sut/alternation-pattern (sut/empty-pattern) (sut/character-pattern \a)))))
    (t/is (= (sut/failure-pattern) (sut/normalize (sut/alternation-pattern)))
          "An alternation with no possibilities is the same as a failure")
    (t/is (= (sut/character-pattern \a)
             (sut/normalize (sut/alternation-pattern (sut/character-pattern \a))))
          "An alternation with one pattern is the same as just the pattern")
    (t/is (= (sut/normalize (sut/alternation-pattern (sut/empty-pattern)
                                                     (sut/character-pattern \a)))
             (sut/normalize (sut/alternation-pattern (sut/empty-pattern)
                                                     (sut/character-pattern \a)
                                                     (sut/failure-pattern))))
          "An alternation with a failure branch is the same as the same alternation without the failure branch")
    (t/is (= (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \a) (sut/character-pattern \b))
                                      (sut/empty-pattern)
                                      (sut/any-character-pattern)
                                      (sut/sequence-pattern (sut/character-pattern \c) (sut/any-character-pattern \d)))
             (sut/normalize (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \a)
                                                                           (sut/character-pattern \b))
                                                     (sut/alternation-pattern (sut/empty-pattern)
                                                                              (sut/any-character-pattern))
                                                     (sut/sequence-pattern (sut/character-pattern \c)
                                                                           (sut/any-character-pattern \d)))))
          "Directly nested alternations are the same as a single alternation with all of the possibilities")
    (t/is (= (sut/sequence-pattern (sut/character-pattern \a)
                                   (sut/alternation-pattern (sut/empty-pattern) (sut/character-pattern \b)))
             (sut/normalize (sut/alternation-pattern (sut/character-pattern \a)
                                                     (sut/sequence-pattern (sut/character-pattern \a)
                                                                           (sut/character-pattern \b)))))
          "When all branches start the same, it is the same as a sequence of that start and an alternation of the ends")
    (t/is (= (sut/alternation-pattern (sut/empty-pattern) (sut/character-pattern \a))
             (sut/normalize (sut/alternation-pattern (sut/empty-pattern)
                                                     (sut/character-pattern \a)
                                                     (sut/character-pattern \a)
                                                     (sut/empty-pattern))))
          "Branches of an alternation can be pruned if they've exactly shown up before")
    (t/is (= (sut/sequence-pattern
              (sut/character-pattern \a)
              (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \b) (sut/character-pattern \c))
                                       (sut/sequence-pattern (sut/character-pattern \d) (sut/character-pattern \e))
                                       (sut/sequence-pattern (sut/character-pattern \f) (sut/character-pattern \g))
                                       (sut/sequence-pattern (sut/character-pattern \h) (sut/character-pattern \i))
                                       (sut/sequence-pattern (sut/character-pattern \j) (sut/character-pattern \k))))
             (sut/alternation-pattern
              (sut/character-pattern \a)
              (sut/sequence-pattern
               (sut/character-pattern \a)
               (sut/alternation-pattern (sut/alternation-pattern)
                                        (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \b)
                                                                                       (sut/character-pattern \c))
                                                                 (sut/sequence-pattern (sut/character-pattern \d)
                                                                                       (sut/character-pattern \e)))
                                        (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \f)
                                                                                       (sut/character-pattern \g))
                                                                 (sut/sequence-pattern (sut/character-pattern \h)
                                                                                       (sut/character-pattern \i)))))
              (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \a)
                                                             (sut/character-pattern \b)
                                                             (sut/character-pattern \c))
                                       (sut/sequence-pattern (sut/character-pattern \a)
                                                             (sut/character-pattern \j)
                                                             (sut/character-pattern \k))))))

    (t/testing "character-pattern"
      (t/is (= (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern))
               (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern) (sut/character-pattern \a)))
            "An alternation that matches a single character and any character matches any character")
      (t/is (= (sut/alternation-pattern (sut/empty-pattern) (sut/character-set-pattern \a \b))
               (sut/alternation-pattern (sut/empty-pattern) (sut/character-pattern \a) (sut/character-pattern \b)))
            "An alternation that matches two different characters matches a character set of those characters"))

    (t/testing "character-set-pattern"
      (t/is (= (sut/character-set-pattern [\a \d] [\f \i] [\k \m])
               (sut/normalize (sut/alternation-pattern (sut/character-set-pattern [\a \d] [\f \h] \k \m)
                                                       (sut/character-set-pattern [\b \c] [\g \i] \l))))
            "Character set patterns in an alternation are unioned together")
      (t/is (= (sut/any-character-pattern)
               (sut/normalize (sut/alternation-pattern (sut/character-set-pattern \a) (sut/any-character-pattern))))
            "A character set pattern in alternation with an any character pattern matches any character")
      (t/is (= (sut/character-set-pattern [\a \c])
               (sut/normalize (sut/alternation-pattern (sut/character-set-pattern [\a \b])
                                                       (sut/character-pattern \c))))
            "A character set pattern in alternation with a character pattern is a character set"))

    (t/testing "negated-character-set-pattern"
      (t/is (= (sut/negated-character-set-pattern [\b \c])
               (sut/normalize (sut/alternation-pattern (sut/negated-character-set-pattern [\a \c])
                                                       (sut/negated-character-set-pattern [\b \d]))))
            "Negated character set patterns in an alternation are intersected together")
      (t/is (= (sut/any-character-pattern)
               (sut/normalize (sut/alternation-pattern (sut/negated-character-set-pattern \a)
                                                       (sut/any-character-pattern))))
            "A negated character set pattern in alternation with an any character pattern matches any character")
      (t/is (= (sut/any-character-pattern)
               (sut/normalize (sut/alternation-pattern (sut/character-set-pattern \a)
                                                       (sut/negated-character-set-pattern \a))))
            "Negated character set A in alternation with character set B is the same as A with B removed")
      (t/is (= (sut/any-character-pattern)
               (sut/normalize (sut/alternation-pattern (sut/character-set-pattern [\a \b])
                                                       (sut/negated-character-set-pattern \a))))
            "Negated character set A in alternation with character set B is the same as A with B removed")
      (t/is (= (sut/negated-character-set-pattern \a [\c \d])
               (sut/normalize (sut/alternation-pattern (sut/negated-character-set-pattern [\a \d])
                                                       (sut/character-set-pattern \b))))
            "Negated character set A in alternation with character set B is the same as A with B removed")))

  (t/testing "pattern->regex"
    (t/is (= "a|b|c" (str (sut/pattern->regex (sut/alternation-pattern (sut/character-set-pattern \a)
                                                                       (sut/character-set-pattern \b)
                                                                       (sut/character-set-pattern \c))))))
    (t/is (= "(?:a|b|c)d"
             (str (sut/pattern->regex (sut/sequence-pattern (sut/alternation-pattern (sut/character-set-pattern \a)
                                                                                     (sut/character-set-pattern \b)
                                                                                     (sut/character-set-pattern \c))
                                                            (sut/character-set-pattern \d))))))))

(t/deftest sequence-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern)
             (sut/match-one (sut/sequence-pattern (sut/character-pattern \b) (sut/character-pattern \a)) \a))
          "#\"ba\" << \\a => failure")
    (t/is (= (sut/character-pattern \b)
             (sut/match-one (sut/sequence-pattern (sut/character-pattern \a) (sut/character-pattern \b)) \a))
          "#\"ab\" << \\a => #\"b\"")
    (t/is (= (sut/failure-pattern)
             (sut/match-one (sut/sequence-pattern (sut/character-pattern \a) (sut/character-pattern \a)) \a))
          "#\"aa\" << \\a => #\"a\"")
    (t/is (= (sut/empty-pattern)
             (sut/match-one (sut/sequence-pattern (sut/alternation-pattern (sut/empty-pattern)
                                                                           (sut/character-pattern \b))
                                                  (sut/character-pattern \a))
                            \a))
          "#\"b?a\" << \\a => #\"\"")
    (t/is (= (sut/alternation-pattern (sut/character-pattern \a) (sut/empty-pattern))
             (sut/match-one (sut/sequence-pattern (sut/alternation-pattern (sut/empty-pattern)
                                                                           (sut/character-pattern \a))
                                                  (sut/character-pattern \a))
                            \a))
          "#\"a?a\" << \\a => #\"a?\""))

  (t/testing "matched?"
    (t/is (false? (sut/sequence-pattern (sut/any-character-pattern) (sut/any-character-pattern))))
    (t/is (false? (sut/sequence-pattern (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern))
                                        (sut/any-character-pattern))))
    (t/is (true? (sut/sequence-pattern (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern))
                                       (sut/alternation-pattern (sut/empty-pattern) (sut/any-character-pattern))))))

  (t/testing "normalize"
    (t/is (= (sut/sequence-pattern (sut/character-pattern \a) (sut/character-pattern \b))
             (sut/normalize (sut/sequence-pattern (sut/character-pattern \a) (sut/character-pattern \b)))))
    (t/is (= (sut/empty-pattern) (sut/normalize (sut/sequence-pattern)))
          "An empty sequence pattern is an empty pattern")
    (t/is (= (sut/character-pattern \b)
             (sut/normalize (sut/sequence-pattern (sut/empty-pattern)
                                                  (sut/character-pattern \a)
                                                  (sut/empty-pattern)
                                                  (sut/character-pattern \b)
                                                  (sut/empty-pattern))))
          "A sequence pattern with empty patterns is the same as the sequence pattern without")
    (t/is (= (sut/failure-pattern)
             (sut/normalize (sut/sequence-pattern (sut/failure-pattern) (sut/character-pattern \a))))
          "A sequence pattern with a failure pattern is a failure pattern")
    (t/is (= (sut/sequence-pattern (sut/character-pattern \a)
                                   (sut/character-pattern \b)
                                   (sut/character-pattern \c))
             (sut/normalize (sut/sequence-pattern (sut/sequence-pattern (sut/character-pattern \a)
                                                                        (sut/character-pattern \b))
                                                  (sut/character-pattern \c))))
          "Nested sequence patterns can be flattened")
    (t/is (= (sut/sequence-pattern (sut/character-pattern \a)
                                   (sut/character-pattern \b)
                                   (sut/character-pattern \c))
             (sut/normalize (sut/sequence-pattern (sut/character-pattern \a)
                                                  (sut/sequence-pattern (sut/character-pattern \b)
                                                                        (sut/character-pattern \c)))))))

  (t/testing "pattern->regex"
    (t/is (= "abc" (str (sut/sequence-pattern (sut/character-pattern \a)
                                              (sut/character-pattern \b)
                                              (sut/character-pattern \c)))))
    (t/is (= "(?:abc)|d" (str (sut/alternation-pattern (sut/sequence-pattern (sut/character-pattern \a)
                                                                             (sut/character-pattern \b)
                                                                             (sut/character-pattern \c))
                                               (sut/character-pattern \d)))))

    (t/testing "repetition-pattern"
      (t/is (= (sut/repetition-pattern (sut/character-set-pattern \a))
               (sut/normalize (sut/sequence-pattern (sut/repetition-pattern (sut/character-set-pattern \a))
                                                    (sut/repetition-pattern (sut/character-set-pattern \a)))))
            "#\"a*a*\" => #\"a*\"")
      (t/is (= (sut/repetition-pattern (sut/character-set-pattern \a))
               (sut/normalize (sut/sequence-pattern (sut/repetition-pattern (sut/character-set-pattern \a))
                                                    (sut/character-set-pattern \a))))
            "#\"a*a\" => #\"aa*\""))))

(t/deftest character-set-pattern-test
  (t/testing "constructor"
    (t/is (= (sut/failure-pattern) (sut/character-set-pattern)))
    (t/is (= (sut/character-set-pattern [\a \a]) (sut/character-set-pattern \a)))
    (t/is (= (sut/character-set-pattern [\a \a] [\c \c]) (sut/character-set-pattern \a \c)))
    (t/is (= (sut/character-set-pattern [\a \a] [\c \c]) (sut/character-set-pattern \c \a)))
    (t/is (= (sut/character-set-pattern [\a \b]) (sut/character-set-pattern \a \b)))
    (t/is (= (sut/character-set-pattern [\a \b]) (sut/character-set-pattern \b \a)))
    (t/is (= (sut/character-set-pattern [\a \e]) (sut/character-set-pattern \e [\b \d] [\a \c]))))

  (t/testing "match-one"
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/character-set-pattern \a) \d)))
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/character-set-pattern \a) \a)))
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/character-set-pattern [\a \c]) \d)))
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/character-set-pattern [\a \c] [\e \g]) \f))))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/character-set-pattern \a)))))

  (t/testing "normalize"
    (t/is (= (sut/character-set-pattern \a) (sut/normalize (sut/character-set-pattern \a)))))

  (t/testing "pattern->regex"
    (t/is (= "a" (str (sut/pattern->regex (sut/character-set-pattern \a)))))
    (t/is (= "[a-b]" (str (sut/pattern->regex (sut/character-set-pattern \a \b)))))))

(t/deftest negated-character-set-pattern-test
  (t/testing "constructor"
    (t/is (= (sut/any-character-pattern) (sut/negated-character-set-pattern)))
    (t/is (= (sut/negated-character-set-pattern [\a \a]) (sut/negated-character-set-pattern \a)))
    (t/is (= (sut/negated-character-set-pattern [\a \a] [\c \c]) (sut/negated-character-set-pattern \a \c)))
    (t/is (= (sut/negated-character-set-pattern [\a \a] [\c \c]) (sut/negated-character-set-pattern \c \a)))
    (t/is (= (sut/negated-character-set-pattern [\a \b]) (sut/negated-character-set-pattern \a \b)))
    (t/is (= (sut/negated-character-set-pattern [\a \b]) (sut/negated-character-set-pattern \b \a)))
    (t/is (= (sut/negated-character-set-pattern [\a \e]) (sut/negated-character-set-pattern \e [\b \d] [\a \c]))))

  (t/testing "match-one"
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/negated-character-set-pattern \a) \d)))
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/negated-character-set-pattern \a) \a)))
    (t/is (= (sut/empty-pattern) (sut/match-one (sut/negated-character-set-pattern [\a \c]) \d)))
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/negated-character-set-pattern [\a \c] [\e \g]) \f))))

  (t/testing "matched?"
    (t/is (false? (sut/matched? (sut/negated-character-set-pattern \a)))))

  (t/testing "normalize"
    (t/is (= (sut/negated-character-set-pattern \a) (sut/normalize (sut/negated-character-set-pattern \a)))))

  (t/testing "pattern->regex"
    (t/is (= "[^a]" (str (sut/pattern->regex (sut/negated-character-set-pattern \a)))))
    (t/is (= "[^a-b]" (str (sut/pattern->regex (sut/negated-character-set-pattern \a \b)))))))

(t/deftest repetition-pattern-test
  (t/testing "match-one"
    (t/is (= (sut/failure-pattern) (sut/match-one (sut/repetition-pattern (sut/character-set-pattern \a)) \b)))
    (t/is (= (sut/repetition-pattern (sut/character-set-pattern \a))
             (sut/match-one (sut/repetition-pattern (sut/character-set-pattern \a)) \a)))
    (t/is (= (sut/sequence-pattern (sut/character-set-pattern \b)
                                   (sut/repetition-pattern (sut/sequence-pattern (sut/character-set-pattern \a)
                                                                                 (sut/character-set-pattern \b))))
             (sut/match-one (sut/repetition-pattern (sut/sequence-pattern (sut/character-set-pattern \a)
                                                                          (sut/character-set-pattern \b)))
                            \a))))

  (t/testing "matched?" (t/is (true? (sut/matched? (sut/repetition-pattern (sut/character-set-pattern \a))))))

  (t/testing "normalize"
    (t/is (= (sut/repetition-pattern (sut/character-set-pattern \a))
             (sut/normalize (sut/repetition-pattern (sut/character-set-pattern \a)))))
    (t/is (= (sut/repetition-pattern (sut/character-set-pattern \a))
             (sut/normalize (sut/repetition-pattern (sut/repetition-pattern (sut/character-set-pattern \a)))))
          "#\"(?:a*)*\" => #\"a*\""))

  (t/testing "pattern->regex"
    (t/is (= "a*" (str (sut/pattern->regex (sut/repetition-pattern (sut/character-set-pattern \a))))))
    (t/is (=
           "(?:ab)*"
           (str (sut/pattern->regex (sut/repetition-pattern (sut/sequence-pattern (sut/character-set-pattern \a)
                                                                                  (sut/character-set-pattern \b)))))))))
