package io.github.andreyfillipe

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("io.github.andreyfillipe")
		.start()
}

