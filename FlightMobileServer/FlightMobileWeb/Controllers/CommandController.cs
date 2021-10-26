using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using FlightMobileWeb.Models;
using Flurl;
using System.Text;
using System.Net;
using System.IO;
using Microsoft.AspNetCore.Server.IIS;
using System.Net.Http;
using System.Web;
using ServiceStack.Host;

namespace FlightMobileWeb.Controllers

{
    [Route("api/[controller]")]
    [ApiController]
    public class CommandController : ControllerBase
    {
        private readonly FlightManager flightManager;
        public CommandController(FlightManager flightManager)
        {
            this.flightManager = flightManager;
        }

        // GET: api/Command
        [HttpGet]
        public IActionResult Get()
        {
            Task<byte[]> result = this.flightManager.Get();
            if (result.Result==null)
            {
                return new NotFoundResult();
            }
            return File(result.Result, "image/jpg");
            


        }

        // POST: api/Command
        [HttpPost]
        public IActionResult Post([FromBody] Command cmd)
        {


            Task<Result> result = this.flightManager.Execute(cmd);
            int answer = (int)result.Result;
            if (answer == 404)
            {
                return  new NotFoundResult() ;
            }
            else if (answer== 500) { return new BadRequestResult(); }
            else
            {
                return new OkObjectResult(200);
            }
            


        }

      

    }
}


