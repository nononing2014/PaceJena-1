function sum {
    param
    (
        [int]$a,
        [int]$b
    )
    $c = $a + $b
    return $c
}

sum -a 2 -b 3
