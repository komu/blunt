(define nil
  (make-nil))

(define foldr
  (lambda (f e l)
    (if (nil? l)
        e
        (f (head l)
           (foldr f e (tail l))))))

(define sum
  (lambda (l)
    (foldr + 0 l)))

(define product
  (lambda (l)
    (foldr * 1 l)))

(define map
  (lambda (f l)
    (foldr (lambda (x y) (cons (f x) y))
           nil
           l)))

(define filter
  (lambda (p l)
    (foldr (lambda (x y) (if (p x) (cons x y) y))
           nil
           l)))

(define length
  (lambda (l)
    (foldr (lambda (x y) (+ 1 y)) 0 l)))

(define square
  (lambda (x) (* x x)))

(define range
  (lambda (min max)
    (if (< min max)
      (cons min (range (+ min 1) max))
      nil)))

(define or
  (lambda (x y)
    (if x true y)))

(define and
  (lambda (x y)
    (if x y false)))

(define not
  (lambda (b)
    (if b false true)))

(define zero?
  (lambda (n)
    (= n 0)))

(define take
  (lambda (n lst)
    (if (or (nil? lst) (zero? n))
       nil
       (cons (head lst) (take (- n 1) (tail lst))))))

(define drop
  (lambda (n lst)
    (if (nil? lst)
      nil
      (if (> n 0)
        (drop (- n 1) (tail lst))
        lst))))

(define compose
  (lambda (f g)
    (lambda (x) (g (f x)))))

(define const
  (lambda (x)
    (lambda (y) x)))

(define all
  (lambda (f l)
    (foldr (lambda (x xs) (and (f x) xs)) true l)))

(define any
  (lambda (f l)
    (foldr (lambda (x xs) (or (f x) xs)) false l)))

(define append
  (lambda (xs ys)
    (foldr cons ys xs)))

(define concat
  (lambda (xs)
    (foldr append nil xs)))

(define concatMap
  (lambda (f xs)
    (concat (map f xs))))

(define flip
  (lambda (f)
    (lambda (x y) (f y x))))

(define curry
  (lambda (f)
    (lambda (x) (lambda (y) (f x y)))))

(define uncurry
  (lambda (f)
    (lambda (x y) ((f x) y))))

(define id
  (lambda (x) x))
