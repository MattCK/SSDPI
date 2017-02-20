<?php

namespace Google\AdsApi\Dfp\v201611;


/**
 * This file was generated from WSDL. DO NOT EDIT.
 */
class getRateCardsByStatementResponse
{

    /**
     * @var \Google\AdsApi\Dfp\v201611\RateCardPage $rval
     */
    protected $rval = null;

    /**
     * @param \Google\AdsApi\Dfp\v201611\RateCardPage $rval
     */
    public function __construct($rval = null)
    {
      $this->rval = $rval;
    }

    /**
     * @return \Google\AdsApi\Dfp\v201611\RateCardPage
     */
    public function getRval()
    {
      return $this->rval;
    }

    /**
     * @param \Google\AdsApi\Dfp\v201611\RateCardPage $rval
     * @return \Google\AdsApi\Dfp\v201611\getRateCardsByStatementResponse
     */
    public function setRval($rval)
    {
      $this->rval = $rval;
      return $this;
    }

}
