using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace FlightMobileWeb.Models
{
    public class AsyncImage
    {
        public Task<byte[]> Task { get => Completion.Task; }
        public TaskCompletionSource<byte[]> Completion { get; private set; }
        public AsyncImage()
        {

            Completion = new TaskCompletionSource<byte[]>(TaskCreationOptions.RunContinuationsAsynchronously);
        }

    }
}
