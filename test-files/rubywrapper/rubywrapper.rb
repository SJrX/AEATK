#!/usr/bin/ruby

$seed = ARGV[4]


output = false
Signal.trap("SIGTERM") {
    #=== Respond to termination by deleting temporary file and crashing.
    begin
        if(output == false)   
          output = true
          puts "Result for ParamILS: CRASHED, 0, 0, 0, #{$seed}, SIGTERM"
        end

        File.delete(tmp_file)
    ensure
        Process.exit 1
    end
}

Signal.trap("SIGINT") {
    #=== Respond to termination by deleting temporary file and crashing.
    begin
        if(output == false)   
          output = true
          puts "Result for ParamILS: CRASHED, 0, 0, 0, #{$seed}, SIGINT"
        end

        File.delete(tmp_file)
    ensure
        Process.exit 1
    end
}
  
Signal.trap("SIGQUIT") {
    #=== Respond to termination by deleting temporary file and crashing.
    begin
        if(output == false)   
          output = true
          puts "Result for ParamILS: CRASHED, 0, 0, 0, #{$seed}, SIGQUIT"
        end

        File.delete(tmp_file)
    ensure
        Process.exit 1
    end
}
  
while true

end