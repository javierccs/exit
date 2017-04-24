package com.serenity.alm.jenkins.util

/**
 * Created by n64168 on 01/04/2017.
 * Groovy scripts utilities
 */
class GroovyScriptUtil {
    /**
     * Executes given groovy with binding
     * @param groovyScriptPath path of groovy script to execute
     * @param printOutput if true script output is printed
     * @param bindingMap Bindings to apply to groovy
     * @return object with:
     *   result: Object returned by script
     *   output: Console output
     */
    public static executeGroovyFile (String groovyScriptPath, boolean printOutput = true, Map bindingMap = [:]){
        GroovyShell shell
        Binding binding
        ByteArrayOutputStream out
        def result
        try {
            out = new ByteArrayOutputStream()
            binding = new Binding()
            binding.setProperty("out", new PrintStream(out))
            shell = new GroovyShell(binding)
            result = [
                result: shell.evaluate(new File(groovyScriptPath)),
                output: out
            ]
            return result
        }finally{
            if (printOutput && result?.output){
                println (result.output)
            }
        }
    }
}
