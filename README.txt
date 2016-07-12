IMPORTANT NOTES:

* RESOURCES

	There is an important caveat while trying to implement this template-based header gov.nist.toolkit.wsseToolkit.generation. 

	 When trying to access a resource within an embedded jar, it is necessary to use getResourceAsStream rather than
	 any other methods.
	 getResourceAsStream is the only method that can resolve jar path.
	 By default, the path is look up starting from the package of the class is it call upon.
	 To start from the jar root, the path must be prepended by a "/".
	 
	 Since this project will be embedded as a jar within TTT, it is crucial to follow this method consistently.


* CUT AND PASTE
	 Some applications will add extra characters that might break the validation.
	 Sublime seems to work better than intellij or eclipse and does this properly.
	 Reformating will be a problem however.