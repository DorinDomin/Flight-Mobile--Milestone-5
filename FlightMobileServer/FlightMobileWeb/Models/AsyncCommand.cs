using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace FlightMobileWeb.Models
{
    public enum Result { Ok = 200, NotOk = 501, serverEroor = 500,notConnect = 404 }
    public class AsyncCommand
    {
        public Command Command { get; private set; }
        public Task<Result> Task { get => Completion.Task; }
        public TaskCompletionSource<Result> Completion { get; private set; }
        public AsyncCommand(Command cmd)
        {
            Command = cmd;
            Completion = new TaskCompletionSource<Result>(TaskCreationOptions.RunContinuationsAsynchronously);
        }
    }
}
