require 'socket'
sources = []
sinks = []
source_server = TCPServer.new(7007)
sink_server = TCPServer.new(7008)

t1 = Thread.new {
    loop do
        Thread.start(source_server.accept) do |con|
            sources << con
            puts "Source Added"
            while true
                begin
                    if msg = con.gets
                        puts "Message received #{msg}"
                        sinks.each do |sink|
                            begin
                                sink.puts(msg)
                            rescue Exception => e
                                puts "Sink lost"
                                sinks = sinks - [sink]
                            end
                            
                    end
                end
                rescue Exception => ex
                    sources = sources - [con]
                    puts "Source removed"
                    con.close
                end
            end
        end
    end
}


t2 = Thread.new {

    loop do
        Thread.start(sink_server.accept) do |con|
            puts "Sink added"
            sinks << con

        end
    end
}

t1.join
t2.join