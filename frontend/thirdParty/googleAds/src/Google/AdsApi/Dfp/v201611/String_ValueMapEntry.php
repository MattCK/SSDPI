<?php

namespace Google\AdsApi\Dfp\v201611;


/**
 * This file was generated from WSDL. DO NOT EDIT.
 */
class String_ValueMapEntry
{

    /**
     * @var string $key
     */
    protected $key = null;

    /**
     * @var \Google\AdsApi\Dfp\v201611\Value $value
     */
    protected $value = null;

    /**
     * @param string $key
     * @param \Google\AdsApi\Dfp\v201611\Value $value
     */
    public function __construct($key = null, $value = null)
    {
      $this->key = $key;
      $this->value = $value;
    }

    /**
     * @return string
     */
    public function getKey()
    {
      return $this->key;
    }

    /**
     * @param string $key
     * @return \Google\AdsApi\Dfp\v201611\String_ValueMapEntry
     */
    public function setKey($key)
    {
      $this->key = $key;
      return $this;
    }

    /**
     * @return \Google\AdsApi\Dfp\v201611\Value
     */
    public function getValue()
    {
      return $this->value;
    }

    /**
     * @param \Google\AdsApi\Dfp\v201611\Value $value
     * @return \Google\AdsApi\Dfp\v201611\String_ValueMapEntry
     */
    public function setValue($value)
    {
      $this->value = $value;
      return $this;
    }

}
