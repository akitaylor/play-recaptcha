/*
 * Copyright 2014 Chris Nappin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nappin.play.recaptcha

import play.api.Logger

/**
 * Used to parse verify API responses.
 * 
 * Follows the API documented at <a href="https://developers.google.com/recaptcha/docs/verify">Verify Without 
 * Plugins</a>, but if it encounters any API errors (e.g. invalid responses) then it returns an error with an
 * artificial error code corresponding to <code>apiError</code>.
 * 
 * @author Chris Nappin
 */
class ResponseParser {
    
    val logger = Logger(this.getClass())
    
    /**
     * Parses a verify response, which is a series of newline-separated strings.
     * @param response		The response to parse
     * @return Either an Error (with a populated error code) or a Success
     */
    def parseResponse(response: String): Either[Error, Success] = {
        if (response.size < 1) {
            // empty response
            logger.error("API Error: response was empty")
            return Left(Error(RecaptchaErrorCode.apiError))
            
        } else {
	        // split by newlines
	        val lines = response.split("\n")
	        
	        if ("true" == lines(0)) {
	            // true denotes the recaptcha response was correct
	            // (Note: we ignore any further lines in the response)
	            logger.info("Response was: success")
	            return Right(Success())
	        
	        } else if ("false" == lines(0) && lines.size > 1) {
	            // false with an error code on the following line denotes an error
	            // (Note: we ignore any further lines in the response)
	            val errorCode = lines(1)
	            logger.info(s"Response was: error => $errorCode")
	            return Left(Error(errorCode))
	        }
	        
	        // anything else doesn't meet the API definition
	        logger.error("Invalid response: " + response)
	        return Left(Error(RecaptchaErrorCode.apiError))
        }
    }
}

/** Signifies the recaptcha response was valid. */
case class Success()

/** Signifies a recaptcha error, such as captcha response was incorrect or a technical error of some kind. */
case class Error(code: String)
