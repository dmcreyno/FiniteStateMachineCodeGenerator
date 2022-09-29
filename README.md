# FiniteStateMachineCodeGenerator
Given an XML definition, generate a class with stubs for the events.

That's the official description. The reality is that this and other things I've posted on github are just busy work to help me stay marginally informed about Java features and to remind me about the stuff I already know.

The program requires two arguments. First, it needs the qualified name of the input XML. This XML defines the state machine using the associated XSD, "fsm.xsd."

The second argument defines the root directory for the generated code. The code will class(es) will be written to a package as defined in the XML definition.