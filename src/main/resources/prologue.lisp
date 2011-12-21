(define nil
  (make-nil))

(define (foldr f e l)
  (if (nil? l)
    e
    (f (head l)
       (foldr f e (tail l)))))

(define (sum l)
  (foldr + 0 l))

(define (product l)
  (foldr * 1 l))

(define (map f l)
  (foldr (lambda (x y) (cons (f x) y))
         nil
         l))

(define (filter p l)
  (foldr (lambda (x y) (if (p x) (cons x y) y))
         nil
         l))

(define (length l)
  (foldr (lambda (x y) (+ 1 y)) 0 l))

(define (square x)
  (* x x))

(define (range min max)
  (if (< min max)
    (cons min (range (+ min 1) max))
    nil))

(define (or x y)
  (if x true y))

(define (and x y)
  (if x y false))

(define (not b)
  (if b false true))

(define (zero? n)
  (= n 0))

(define (take n lst)
  (if (or (nil? lst) (zero? n))
     nil
     (cons (head lst) (take (- n 1) (tail lst)))))

(define (drop n lst)
  (if (nil? lst)
    nil
    (if (> n 0)
      (drop (- n 1) (tail lst))
      lst)))

(define (compose f g)
  (lambda (x) (g (f x))))

(define (const x)
  (lambda (y) x))

(define (all p l)
  (foldr (lambda (x xs) (and (p x) xs)) true l))

(define (any p l)
  (foldr (lambda (x xs) (or (p x) xs)) false l))

(define (append xs ys)
  (foldr cons ys xs))

(define (concat xs)
  (foldr append nil xs))

(define (concatMap f xs)
  (concat (map f xs)))

(define (flip f)
  (lambda (x y) (f y x)))

(define (curry f)
  (lambda (x) (lambda (y) (f x y))))

(define (uncurry f)
  (lambda (x y) ((f x) y)))

(define (id x)
  x)
