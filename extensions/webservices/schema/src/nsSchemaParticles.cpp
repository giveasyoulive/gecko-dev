/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Mozilla.
 * 
 * The Initial Developer of the Original Code is Netscape
 * Communications.  Portions created by Netscape Communications are
 * Copyright (C) 2001 by Netscape Communications.  All
 * Rights Reserved.
 * 
 * Contributor(s): 
 *   Vidur Apparao <vidur@netscape.com> (original author)
 */

#include "nsSchemaPrivate.h"

////////////////////////////////////////////////////////////
//
// nsSchemaParticleBase implementation
//
////////////////////////////////////////////////////////////
nsSchemaParticleBase::nsSchemaParticleBase(nsISchema* aSchema)
  : nsSchemaComponentBase(aSchema), mMinOccurs(1), mMaxOccurs(1)
{
}

nsSchemaParticleBase::~nsSchemaParticleBase()
{
}

NS_IMETHODIMP 
nsSchemaParticleBase::GetMinOccurs(PRUint32 *aMinOccurs)
{
  NS_ENSURE_ARG_POINTER(aMinOccurs);

  *aMinOccurs = mMinOccurs;

  return NS_OK;
}
 
NS_IMETHODIMP
nsSchemaParticleBase::GetMaxOccurs(PRUint32 *aMaxOccurs)
{
  NS_ENSURE_ARG_POINTER(aMaxOccurs);

  *aMaxOccurs = mMaxOccurs;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaParticleBase::SetMinOccurs(PRUint32 aMinOccurs)
{
  mMinOccurs = aMinOccurs;

  if (mMaxOccurs < mMinOccurs) {
    mMaxOccurs = mMinOccurs;
  }

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaParticleBase::SetMaxOccurs(PRUint32 aMaxOccurs)
{
  mMaxOccurs = aMaxOccurs;

  if (mMinOccurs > mMaxOccurs) {
    mMinOccurs = mMaxOccurs;
  }

  return NS_OK;
}

////////////////////////////////////////////////////////////
//
// nsSchemaModelGroup implementation
//
////////////////////////////////////////////////////////////
nsSchemaModelGroup::nsSchemaModelGroup(nsISchema* aSchema, 
                                       const nsAReadableString& aName,
                                       PRUint16 aCompositor)
  : nsSchemaParticleBase(aSchema), mName(aName), mCompositor(aCompositor)
{
  NS_INIT_ISUPPORTS();
}

nsSchemaModelGroup::~nsSchemaModelGroup()
{
}

NS_IMPL_ISUPPORTS3(nsSchemaModelGroup, 
                   nsISchemaComponent,
                   nsISchemaParticle,
                   nsISchemaModelGroup)


/* void resolve (); */
NS_IMETHODIMP 
nsSchemaModelGroup::Resolve()
{
  if (mIsResolving) {
    return NS_OK;
  }

  mIsResolving = PR_TRUE;
  nsresult rv;
  PRUint32 i, count;

  mParticles.Count(&count);
  for (i = 0; i < count; i++) {
    nsCOMPtr<nsISchemaParticle> particle;
    
    rv = mParticles.QueryElementAt(i, NS_GET_IID(nsISchemaParticle),
                                   getter_AddRefs(particle));
    if (NS_SUCCEEDED(rv)) {
      rv = particle->Resolve();
      if (NS_FAILED(rv)) {
        mIsResolving = PR_FALSE;
        return rv;
      }
    }
  }
  mIsResolving = PR_FALSE;

  return NS_OK;
}

/* void clear (); */
NS_IMETHODIMP 
nsSchemaModelGroup::Clear()
{
  if (mIsClearing) {
    return NS_OK;
  }

  mIsClearing = PR_TRUE;
  nsresult rv;
  PRUint32 i, count;

  mParticles.Count(&count);
  for (i = 0; i < count; i++) {
    nsCOMPtr<nsISchemaParticle> particle;
    
    rv = mParticles.QueryElementAt(i, NS_GET_IID(nsISchemaParticle),
                                   getter_AddRefs(particle));
    if (NS_SUCCEEDED(rv)) {
      particle->Clear();
    }
  }
  mIsClearing = PR_FALSE;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaModelGroup::GetParticleType(PRUint16 *aParticleType)
{
  NS_ENSURE_ARG_POINTER(aParticleType);

  *aParticleType = nsISchemaParticle::PARTICLE_TYPE_MODEL_GROUP;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaModelGroup::GetName(nsAWritableString& aName)
{
  aName.Assign(mName);

  return NS_OK;
}

/* readonly attribute unsigned short compositor; */
NS_IMETHODIMP 
nsSchemaModelGroup::GetCompositor(PRUint16 *aCompositor)
{
  NS_ENSURE_ARG_POINTER(aCompositor);

  *aCompositor = mCompositor;

  return NS_OK;
}

/* readonly attribute PRUint32 particleCount; */
NS_IMETHODIMP 
nsSchemaModelGroup::GetParticleCount(PRUint32 *aParticleCount)
{
  NS_ENSURE_ARG_POINTER(aParticleCount);

  return mParticles.Count(aParticleCount);
}

/* nsISchemaParticle getParticle (in PRUint32 index); */
NS_IMETHODIMP 
nsSchemaModelGroup::GetParticle(PRUint32 index, nsISchemaParticle **_retval)
{
  NS_ENSURE_ARG_POINTER(_retval);

  return mParticles.QueryElementAt(index, NS_GET_IID(nsISchemaParticle),
                                   (void**)_retval);
}

NS_IMETHODIMP 
nsSchemaModelGroup::AddParticle(nsISchemaParticle* aParticle)
{
  NS_ENSURE_ARG_POINTER(aParticle);

  return mParticles.AppendElement(aParticle);
}

////////////////////////////////////////////////////////////
//
// nsSchemaModelGroupRef implementation
//
////////////////////////////////////////////////////////////
nsSchemaModelGroupRef::nsSchemaModelGroupRef(nsISchema* aSchema,
                                             const nsAReadableString& aRef)
  : nsSchemaParticleBase(aSchema), mRef(aRef)
{
  NS_INIT_ISUPPORTS();
}

nsSchemaModelGroupRef::~nsSchemaModelGroupRef()
{
}

NS_IMPL_ISUPPORTS3(nsSchemaModelGroupRef, 
                   nsISchemaComponent,
                   nsISchemaParticle,
                   nsISchemaModelGroup)

/* void resolve (); */
NS_IMETHODIMP 
nsSchemaModelGroupRef::Resolve()
{
  nsresult rv = NS_OK;

  if (mIsResolving) {
    return NS_OK;
  }

  mIsResolving = PR_TRUE;
  if (!mModelGroup && mSchema) {
    mSchema->GetModelGroupByName(mRef, getter_AddRefs(mModelGroup));
  }

  if (mModelGroup) {
    rv = mModelGroup->Resolve();
  }
  mIsResolving = PR_FALSE;

  return rv;
}

/* void clear (); */
NS_IMETHODIMP 
nsSchemaModelGroupRef::Clear()
{
  if (mIsClearing) {
    return NS_OK;
  }

  mIsClearing = PR_TRUE;
  if (mModelGroup) {
    mModelGroup->Clear();
    mModelGroup = nsnull;
  }
  mIsClearing = PR_FALSE;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaModelGroupRef::GetParticleType(PRUint16 *aParticleType)
{
  NS_ENSURE_ARG_POINTER(aParticleType);

  *aParticleType = nsISchemaParticle::PARTICLE_TYPE_MODEL_GROUP;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaModelGroupRef::GetName(nsAWritableString& aName)
{
  if (!mModelGroup) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mModelGroup->GetName(aName);
}


/* readonly attribute unsigned short compositor; */
NS_IMETHODIMP 
nsSchemaModelGroupRef::GetCompositor(PRUint16 *aCompositor)
{
  NS_ENSURE_ARG_POINTER(aCompositor);

  if (!mModelGroup) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mModelGroup->GetCompositor(aCompositor);
}

/* readonly attribute PRUint32 particleCount; */
NS_IMETHODIMP 
nsSchemaModelGroupRef::GetParticleCount(PRUint32 *aParticleCount)
{
  NS_ENSURE_ARG_POINTER(aParticleCount);
  
  if (!mModelGroup) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mModelGroup->GetParticleCount(aParticleCount);  
}

/* nsISchemaParticle getParticle (in PRUint32 index); */
NS_IMETHODIMP 
nsSchemaModelGroupRef::GetParticle(PRUint32 index, nsISchemaParticle **_retval)
{
  NS_ENSURE_ARG_POINTER(_retval);
  
  if (!mModelGroup) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mModelGroup->GetParticle(index, _retval);
}

////////////////////////////////////////////////////////////
//
// nsSchemaAnyParticle implementation
//
////////////////////////////////////////////////////////////
nsSchemaAnyParticle::nsSchemaAnyParticle(nsISchema* aSchema)
  : nsSchemaParticleBase(aSchema), mProcess(PROCESS_STRICT)
{
  NS_INIT_ISUPPORTS();
}

nsSchemaAnyParticle::~nsSchemaAnyParticle()
{
}

NS_IMPL_ISUPPORTS3(nsSchemaAnyParticle, 
                   nsISchemaComponent,
                   nsISchemaParticle,
                   nsISchemaAnyParticle)


/* void resolve (); */
NS_IMETHODIMP 
nsSchemaAnyParticle::Resolve()
{
  return NS_OK;
}

/* void clear (); */
NS_IMETHODIMP 
nsSchemaAnyParticle::Clear()
{
  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaAnyParticle::GetParticleType(PRUint16 *aParticleType)
{
  NS_ENSURE_ARG_POINTER(aParticleType);

  *aParticleType = nsISchemaParticle::PARTICLE_TYPE_ANY;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaAnyParticle::GetName(nsAWritableString& aName)
{
  aName.Assign(NS_LITERAL_STRING("any"));

  return NS_OK;
}

/* readonly attribute unsigned short process; */
NS_IMETHODIMP 
nsSchemaAnyParticle::GetProcess(PRUint16 *aProcess)
{
  NS_ENSURE_ARG_POINTER(aProcess);
  
  *aProcess = mProcess;

  return NS_OK;
}

/* readonly attribute AString namespace; */
NS_IMETHODIMP 
nsSchemaAnyParticle::GetNamespace(nsAWritableString & aNamespace)
{
  aNamespace.Assign(mNamespace);

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaAnyParticle::SetProcess(PRUint16 aProcess)
{
  mProcess = aProcess;
  
  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaAnyParticle::SetNamespace(const nsAReadableString& aNamespace)
{
  mNamespace.Assign(aNamespace);

  return NS_OK;
}

////////////////////////////////////////////////////////////
//
// nsSchemaElement implementation
//
////////////////////////////////////////////////////////////
nsSchemaElement::nsSchemaElement(nsISchema* aSchema, 
                                 const nsAReadableString& aName)
  : nsSchemaParticleBase(aSchema), mName(aName), 
    mNillable(PR_FALSE), mAbstract(PR_FALSE)
{
  NS_INIT_ISUPPORTS();
}

nsSchemaElement::~nsSchemaElement()
{
}

NS_IMPL_ISUPPORTS3(nsSchemaElement, 
                   nsISchemaComponent,
                   nsISchemaParticle,
                   nsISchemaElement)

/* void resolve (); */
NS_IMETHODIMP 
nsSchemaElement::Resolve()
{
  if (mIsResolving) {
    return NS_OK;
  }

  mIsResolving = PR_TRUE;
  nsresult rv = mType->Resolve();
  mIsResolving = PR_FALSE;

  return rv;
}

/* void clear (); */
NS_IMETHODIMP 
nsSchemaElement::Clear()
{
  if (mIsClearing) {
    return NS_OK;
  }

  mIsClearing = PR_TRUE;
  mType->Clear();
  mType = nsnull;
  mIsClearing = PR_FALSE;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaElement::GetParticleType(PRUint16 *aParticleType)
{
  NS_ENSURE_ARG_POINTER(aParticleType);

  *aParticleType = nsISchemaParticle::PARTICLE_TYPE_ELEMENT;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaElement::GetName(nsAWritableString& aName)
{
  aName.Assign(mName);

  return NS_OK;
}

/* readonly attribute nsISchemaType type; */
NS_IMETHODIMP 
nsSchemaElement::GetType(nsISchemaType * *aType)
{
  NS_ENSURE_ARG_POINTER(aType);
  
  *aType = mType;
  NS_IF_ADDREF(*aType);

  return NS_OK;
}

/* readonly attribute AString defaultValue; */
NS_IMETHODIMP 
nsSchemaElement::GetDefaultValue(nsAWritableString & aDefaultValue)
{
  aDefaultValue.Assign(mDefaultValue);
  
  return NS_OK;
}

/* readonly attribute AString fixedValue; */
NS_IMETHODIMP 
nsSchemaElement::GetFixedValue(nsAWritableString & aFixedValue)
{
  aFixedValue.Assign(mFixedValue);
  
  return NS_OK;
}

/* readonly attribute boolean nillable; */
NS_IMETHODIMP 
nsSchemaElement::GetNillable(PRBool *aNillable)
{
  NS_ENSURE_ARG_POINTER(aNillable);

  *aNillable = mNillable;

  return NS_OK;
}

/* readonly attribute boolean abstract; */
NS_IMETHODIMP 
nsSchemaElement::GetAbstract(PRBool *aAbstract)
{
  NS_ENSURE_ARG_POINTER(aAbstract);

  *aAbstract = mAbstract;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaElement::SetType(nsISchemaType* aType)
{
  NS_ENSURE_ARG_POINTER(aType);

  mType = aType;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaElement::SetConstraints(const nsAReadableString& aDefaultValue,
                                const nsAReadableString& aFixedValue)
{
  mDefaultValue.Assign(aDefaultValue);
  mFixedValue.Assign(aFixedValue);

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaElement::SetFlags(PRBool aNillable, PRBool aAbstract)
{
  mNillable = aNillable;
  mAbstract = aAbstract;

  return NS_OK;
}

////////////////////////////////////////////////////////////
//
// nsSchemaElementRef implementation
//
////////////////////////////////////////////////////////////
nsSchemaElementRef::nsSchemaElementRef(nsISchema* aSchema, 
                                       const nsAReadableString& aRef)
  : nsSchemaParticleBase(aSchema), mRef(aRef)
{
  NS_INIT_ISUPPORTS();
}

nsSchemaElementRef::~nsSchemaElementRef()
{
}

NS_IMPL_ISUPPORTS3(nsSchemaElementRef, 
                   nsISchemaComponent,
                   nsISchemaParticle,
                   nsISchemaElement)

/* void resolve (); */
NS_IMETHODIMP 
nsSchemaElementRef::Resolve()
{
  nsresult rv = NS_OK;
  if (mIsResolving) {
    return NS_OK;
  }

  mIsResolving = PR_TRUE;
  if (!mElement && mSchema) {
    mSchema->GetElementByName(mRef, getter_AddRefs(mElement));
  }

  if (mElement) {
    rv = mElement->Resolve();
  }
  mIsResolving = PR_FALSE;

  return rv;
}

/* void clear (); */
NS_IMETHODIMP 
nsSchemaElementRef::Clear()
{
  if (mIsClearing) {
    return NS_OK;
  }

  mIsClearing = PR_TRUE;
  mElement->Clear();
  mElement = nsnull;
  mIsClearing = PR_FALSE;

  return NS_OK;
}

NS_IMETHODIMP 
nsSchemaElementRef::GetParticleType(PRUint16 *aParticleType)
{
  NS_ENSURE_ARG_POINTER(aParticleType);

  *aParticleType = nsISchemaParticle::PARTICLE_TYPE_ELEMENT;

  return NS_OK;
}

NS_IMETHODIMP
nsSchemaElementRef::GetName(nsAWritableString& aName)
{
  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetName(aName);
}

/* readonly attribute nsISchemaType type; */
NS_IMETHODIMP 
nsSchemaElementRef::GetType(nsISchemaType * *aType)
{
  NS_ENSURE_ARG_POINTER(aType);
  
  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetType(aType);
}

/* readonly attribute AString defaultValue; */
NS_IMETHODIMP 
nsSchemaElementRef::GetDefaultValue(nsAWritableString & aDefaultValue)
{
  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetDefaultValue(aDefaultValue);
}

/* readonly attribute AString fixedValue; */
NS_IMETHODIMP 
nsSchemaElementRef::GetFixedValue(nsAWritableString & aFixedValue)
{
  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetFixedValue(aFixedValue);
}

/* readonly attribute boolean nillable; */
NS_IMETHODIMP 
nsSchemaElementRef::GetNillable(PRBool *aNillable)
{
  NS_ENSURE_ARG_POINTER(aNillable);

  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetNillable(aNillable);
}

/* readonly attribute boolean abstract; */
NS_IMETHODIMP 
nsSchemaElementRef::GetAbstract(PRBool *aAbstract)
{
  NS_ENSURE_ARG_POINTER(aAbstract);

  if (!mElement) {
    return NS_ERROR_NOT_INITIALIZED;
  }

  return mElement->GetAbstract(aAbstract);
}

