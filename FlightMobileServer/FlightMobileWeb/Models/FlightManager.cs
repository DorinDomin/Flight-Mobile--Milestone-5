using System;
using System.Net.Sockets;
using System.Threading.Tasks;
using System.Collections.Concurrent;

using System.Net;
using System.IO;
using Flurl;

using Microsoft.Extensions.Options;
using System.Net.Http;

namespace FlightMobileWeb.Models
{
    public class FlightManager
    {
        readonly BlockingCollection<AsyncCommand> c = null;
        readonly BlockingCollection<AsyncImage> d = null;

        private readonly ServerInfo serverInfo;
        private readonly NetworkStream stream;


        public async Task<byte[]> Get()
        {
            byte[] imageBytes;
            try
            {

                string imageUrl = this.serverInfo.HttpAddress + "/screenshot";

               

                using var client = new HttpClient();
                HttpResponseMessage responseMessage = await client.GetAsync(imageUrl);
                imageBytes = await responseMessage.Content.ReadAsByteArrayAsync();
                return imageBytes;
            }

            catch ( Exception e)
            {
                Console.WriteLine("ArgumentNullException: {0}", e);
                return null;
            }


        }



        public FlightManager(IOptions<ServerInfo> server)
        {
           
            this.serverInfo = server.Value;
            c = new BlockingCollection<AsyncCommand>();
            d = new BlockingCollection<AsyncImage>();
            this.stream = Connect(this.serverInfo.IP, this.serverInfo.Port);
            FirstConnection();
            Start();
        }
       
        private void Start()
        {
            Task.Factory.StartNew(CommandHandler);
        }
        private void FirstConnection()
        {
            Byte[] data = System.Text.Encoding.ASCII.GetBytes("data\r\n");
            if(this.stream!=null)
            this.stream.Write(data, 0, data.Length);//5402

        }
        private void Write(string massage, NetworkStream stream)
        {
            // Translate the passed message into ASCII and store it as a Byte array.
            Byte[] data = System.Text.Encoding.ASCII.GetBytes(massage);
            // Send the message to the connected TcpServer.
            if(this.stream!=null)
            stream.Write(data, 0, data.Length);
        }
        private string WriteAndRead(string massage, NetworkStream stream)
        {
            if (stream == null)
            {
                return "error";
            }
            // Translate the passed message into ASCII and store it as a Byte array.
            Byte[] data = System.Text.Encoding.ASCII.GetBytes(massage);
            // Send the message to the connected TcpServer.
            stream.Write(data, 0, data.Length);

            // Receive the TcpServer.response.

            // Buffer to store the response bytes.
            data = new Byte[256];

            // String to store the response ASCII representation.
            String responseData;

            // Read the first batch of the TcpServer response bytes.
            Int32 bytes = stream.Read(data, 0, data.Length);
            responseData = System.Text.Encoding.ASCII.GetString(data, 0, bytes);

            return responseData;
        }
        static NetworkStream Connect(String server, Int32 postPort)
        {
            try
            {

                // Create a TcpClient.
                // Note, for this client to work you need to have a TcpServer
                // connected to the same address as specified by the server, port
                // combination.

                TcpClient postClient = new TcpClient(server, postPort);
                // Get a client stream for reading and writing.
                //  Stream stream = client.GetStream();

                NetworkStream stream = postClient.GetStream();

                return stream;
            }
            catch (ArgumentNullException e)
            {
                Console.WriteLine("ArgumentNullException: {0}", e);
            }
            catch (SocketException e)
            {
                Console.WriteLine("SocketException: {0}", e);
            }
            return null;
        }

        private void CommandHandler()
        {
            foreach (AsyncCommand async in c.GetConsumingEnumerable())
            {
                if (this.stream == null)
                {
                    async.Completion.SetResult(Result.notConnect);
                    break;
                }
                double DoubleAnswer;
                int error = 0;
                Command cmd = async.Command;
                try
                {
                    Write("set /controls/flight/aileron " + cmd.Aileron.ToString() + " \r\n", stream);
                    string answer = WriteAndRead("get /controls/flight/aileron \r\n", stream);
                    answer = answer[0..^2];
                    DoubleAnswer = Double.Parse(answer);
                    if (DoubleAnswer != cmd.Aileron)
                    {
                        error = 1;
                    }
                    Write("set /controls/flight/rudder " + cmd.Rudder.ToString() + " \r\n", stream);
                    answer = WriteAndRead("get /controls/flight/rudder \r\n", stream);
                    answer = answer[0..^2];
                    DoubleAnswer = Double.Parse(answer);
                    if (DoubleAnswer != cmd.Rudder)
                    {
                       
                        error = 1;
                    }
                    Write("set /controls/flight/elevator " + cmd.Elevator.ToString() + " \r\n", stream);
                    answer = WriteAndRead("get /controls/flight/elevator \r\n", stream);
                    answer = answer[0..^2];
                    DoubleAnswer = Double.Parse(answer);
                    if (DoubleAnswer != cmd.Elevator)
                    {
                        
                        error = 1;
                    }
                    Write("set /controls/engines/current-engine/throttle " + cmd.Throttle.ToString() + " \r\n", stream);
                    answer = WriteAndRead("get /controls/engines/current-engine/throttle \r\n", stream);
                    answer = answer[0..^2];
                    DoubleAnswer = Double.Parse(answer);
                    if (DoubleAnswer != cmd.Throttle)
                    {
                       
                        error = 1;
                    }

                }
                catch (Exception)
                {
                    async.Completion.SetResult(Result.serverEroor);
                    error = 1;
                    return;
                }



                if (error == 1)
                {
                    async.Completion.SetResult(Result.NotOk);
                }
                else
                {
                    async.Completion.SetResult(Result.Ok);
                }

                

            }

        }
       
        public Task<Result> Execute(Command cmd)
        {
            var asyncCommand = new AsyncCommand(cmd);
            c.Add(asyncCommand);
            return asyncCommand.Task;
        }


    }
}
